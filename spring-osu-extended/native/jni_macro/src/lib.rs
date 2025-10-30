use proc_macro::TokenStream;
use proc_macro2::Ident;
use quote::{format_ident, quote};
use syn::{parse_macro_input, ItemFn, LitStr, ReturnType};

#[proc_macro_attribute]
pub fn jni_fn(attr: TokenStream, input: TokenStream) -> TokenStream {
    let input = parse_macro_input!(input as ItemFn);
    let class_path = parse_macro_input!(attr as LitStr).value();
    let fn_name = &input.sig.ident;
    let new_fn_name_str = format!("Java_{}_{}", class_path.replace('.', "_"), fn_name);
    let new_fn_name = Ident::new(&new_fn_name_str, fn_name.span());

    let block = &input.block;
    let inputs = &input.sig.inputs;
    let output = &input.sig.output;

    let output = quote! {
        #[no_mangle]
        pub extern "system" fn #new_fn_name(#inputs) #output #block
    };

    TokenStream::from(output)
}

#[proc_macro_attribute]
pub fn jni_fn_new(attr: TokenStream, item: TokenStream) -> TokenStream {
    let input_fn = parse_macro_input!(item as ItemFn);
    let class_path = parse_macro_input!(attr as LitStr).value();

    let fn_name = &input_fn.sig.ident;
    let new_fn_name_str = format!("Java_{}_{}", class_path.replace('.', "_"), fn_name);
    let new_fn_name = format_ident!("{}", new_fn_name_str);

    let inputs = &input_fn.sig.inputs;
    let stmts = &input_fn.block.stmts;
    let (output_ty, default_return) = match &input_fn.sig.output {
        ReturnType::Type(_, ty) => (quote! { #ty }, quote! { <#ty as Default>::default() }),
        ReturnType::Default => (quote! { () }, quote! { () }),
    };

    let wrapped = quote! {
        match ::std::panic::catch_unwind(|| {
            #(#stmts)*
        }) {
            Ok(result) => { result }
            Err(_) => {
                
                if env.exception_check().expect("check error.") {
                    env.exception_describe().expect("show");
                }
                env.throw_new("Ljava/lang/Exception;", "Rust panic!")
                    .expect("rust panic!");
                #default_return
            }
        }
    };

    let output = quote! {
        #[no_mangle]
        pub extern "system" fn #new_fn_name(#inputs) -> #output_ty {
            #wrapped
        }
    };

    output.into()
}

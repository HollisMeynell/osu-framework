use proc_macro::TokenStream;
use proc_macro2::Ident;
use quote::quote;
use syn::{parse_macro_input, ItemFn, LitStr};

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

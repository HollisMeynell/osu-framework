use crate::{NativeError, Result};
use bytes::BufMut;

enum JniType {
    Byte(u8),
    Int(i32),
    Float(f32),
    Long(i64),
    Double(f64),
    String(String),
}
impl JniType {
    fn get_value(&self) -> u8 {
        match self {
            JniType::Byte(_) => 1 << 0,
            JniType::Int(_) => 1 << 1,
            JniType::Float(_) => 1 << 2,
            JniType::Long(_) => 1 << 3,
            JniType::Double(_) => 1 << 4,
            JniType::String(_) => 1 << 5,
        }
    }
}

pub trait JniPackage: Default + Sized {
    fn size(&self) -> usize;
    fn write(&self, writer: &mut dyn BufMut) -> Result<()>;
    fn read(data: &[u8]) -> Result<Self>;
}


impl JniPackage for String {
    fn size(&self) -> usize {
        self.len()
    }

    fn write(&self, writer: &mut dyn BufMut) -> Result<()> {
        Ok(())
    }

    fn read(data: &[u8]) -> Result<Self> {
        let str_data = data.to_vec();
        let str = String::from_utf8(str_data)
            .or_else(|x| {
                Err(NativeError::Any("".to_string()))
            })?;
        Ok(str)
    }
}

struct Cache {
    size: usize,
    now: usize,
    mem: [i32; 2],
}

impl Default for Cache {
    fn default() -> Self {
        Cache {
            size: 10,
            now: 0,
            mem: [0, 1],
        }
    }
}

impl Iterator for Cache {
    type Item = i32;

    fn next(&mut self) -> Option<Self::Item> {
        if self.now > self.size {
            return None;
        }
        let result = match self.now {
            0 => 0,
            1 => 1,
            _ => {
                let data = self.mem[0] + self.mem[1];
                self.mem[self.now % 2] = data;
                data
            }
        };
        self.now += 1;
        Some(result)
    }
}


#[test]
fn x() {
    let c = Cache::default();
    c
        .skip(5)
        .take(5)
        .enumerate()
        .for_each(|(i, x)| {
           println!("{i} -> {x}")
        });
}
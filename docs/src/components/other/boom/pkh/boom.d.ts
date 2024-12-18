/* tslint:disable */
/* eslint-disable */
export class Runtime {
  private constructor();
  free(): void;
  static new(): Runtime;
  push_ball_box(x: number, y: number): void;
  update(ctx: CanvasRenderingContext2D, time: number): void;
}

export type InitInput = RequestInfo | URL | Response | BufferSource | WebAssembly.Module;

export interface InitOutput {
  readonly memory: WebAssembly.Memory;
  readonly __wbg_runtime_free: (a: number, b: number) => void;
  readonly runtime_new: () => number;
  readonly runtime_push_ball_box: (a: number, b: number, c: number) => void;
  readonly runtime_update: (a: number, b: number, c: number) => void;
  readonly __wbindgen_export_0: (a: number) => void;
}

export type SyncInitInput = BufferSource | WebAssembly.Module;
/**
* Instantiates the given `module`, which can either be bytes or
* a precompiled `WebAssembly.Module`.
*
* @param {{ module: SyncInitInput }} module - Passing `SyncInitInput` directly is deprecated.
*
* @returns {InitOutput}
*/
export function initSync(module: { module: SyncInitInput } | SyncInitInput): InitOutput;

/**
* If `module_or_path` is {RequestInfo} or {URL}, makes a request and
* for everything else, calls `WebAssembly.instantiate` directly.
*
* @param {{ module_or_path: InitInput | Promise<InitInput> }} module_or_path - Passing `InitInput` directly is deprecated.
*
* @returns {Promise<InitOutput>}
*/
export default function __wbg_init (module_or_path?: { module_or_path: InitInput | Promise<InitInput> } | InitInput | Promise<InitInput>): Promise<InitOutput>;

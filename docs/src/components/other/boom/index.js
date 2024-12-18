import init, {Runtime} from "./pkh";

export default async function clickBoomEffect() {
    await init();
    const runtime = Runtime.new();
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");

    document.body.appendChild(canvas);
    canvas.setAttribute("style", "width: 100%; height: 100%; top: 0; left: 0; z-index: 99999; position: fixed; pointer-events: none;");

    if (!ctx || !window.addEventListener) {
        console.warn("canvas or addEventListener is unsupported!");
        return () => {};
    }

    // 随机产生爆炸
    function randomBoom() {
        let timeout = 0;
        let boom = false;
        const boomRegister = () => {
            if (!boom) {
                boom = true;
                // 随机爆实现为每隔一段时间添加一次性事件监听
                window.addEventListener("mousedown", function (e) {
                    runtime.push_ball_box(e.clientX, e.clientY);
                    boom = false;
                }, {once: true});
            }
            timeout = setTimeout(boomRegister, 5 * 1000 + 15 * 1000 * Math.random());
        }
        boomRegister();
        return () => {
            boom = true;
            clearTimeout(timeout);
        }
    }

    // 每次点击都爆炸
    function alwaysBoom() {
        const down = (e) => {
            runtime.push_ball_box(e.clientX, e.clientY);
        }
        window.addEventListener("mousedown", down, false);

        return () => {
            window.removeEventListener("mousedown", down);
        }
    }

    // 页面大小变化时更新 canvas 大小, 添加到 window.resize 事件中
    function updateSize() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
        canvas.style.width = window.innerWidth + 'px';
        canvas.style.height = window.innerHeight + 'px';
    }

    updateSize();
    window.addEventListener("resize", updateSize);
    const remove = alwaysBoom();

    function loop(offset) {
        runtime.update(ctx, offset);
        requestAnimationFrame(loop);
    }

    requestAnimationFrame(loop)

    return () => {
        window.removeEventListener('resize', updateSize);
        remove();
    }
}

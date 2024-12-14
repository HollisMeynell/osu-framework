
// 性能太差, 不如不要
export default function clickBoomEffect() {
    const randomKey = 'EZp9H8oTHv';
    if (document.body.classList.contains(randomKey)) return;
    document.body.classList.add(randomKey);
    let option = true;

    let balls = [];
    let longPressed = false;
    let longPress;
    let multiplier = 5;
    let width, height;
    let ctx;
    const colours = ["#F73859", "#14FFEC", "#00E0FF", "#FF99FE", "#FAF15D"];
    const canvas = document.createElement("canvas");
    init();

    function init() {
        document.body.appendChild(canvas);
        canvas.setAttribute("style", "width: 100%; height: 100%; top: 0; left: 0; z-index: 99999; position: fixed; pointer-events: none;");
    }

    let removeEventListener = () => {
    };
    if (canvas.getContext && window.addEventListener) {
        removeEventListener = addEventListener();
    } else {
        console.warn("canvas or addEventListener is unsupported!");
    }

    function randomBoom() {
        let timeout = 0;
        let boom = false;
        const boomRegister = () => {
            if (!option) return;
            if (!boom) {
                boom = true;
                window.addEventListener("mousedown", function (e) {
                    if (!check(e.target)) return;
                    pushBalls(randBetween(6, 9), e.clientX, e.clientY);
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

    function alwaysBoom() {
        const down = (e) => {
            if (!check(e.target)) return;
            pushBalls(randBetween(6, 9), e.clientX, e.clientY);
            longPress = setTimeout(function () {
                longPressed = true;
            }, 500);
        }
        const up = (e) => {
            clearInterval(longPress);
            if (longPressed === true) {
                pushBalls(randBetween(6, 9 + Math.ceil(multiplier)), e.clientX, e.clientY);
                longPressed = false;
            }
        }
        window.addEventListener("mousedown", down, false);
        window.addEventListener("mouseup", up, false);

        return () => {
            window.removeEventListener("mousedown", down);
            window.removeEventListener("mouseup", up);
        }
    }

    function addEventListener() {
        ctx = canvas.getContext("2d");
        updateSize();
        const remove = alwaysBoom();
        window.addEventListener('resize', updateSize, false);
        requestAnimationFrame(loop);
        return () => {
            window.removeEventListener('resize', updateSize);
            remove()
            option = false;
        }
    }


    function updateSize() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
        canvas.style.width = window.innerWidth + 'px';
        canvas.style.height = window.innerHeight + 'px';
    }

    class Ball {
        constructor(x, y) {
            this.x = x;
            this.y = y;
            this.angle = Math.PI * 2 * Math.random();
            if (longPressed) {
                this.multiplier = randBetween(5, 9 + (multiplier / 2));
            } else {
                this.multiplier = randBetween(5, 9);
            }
            this.vx = (this.multiplier + Math.random() * 0.5) * Math.cos(this.angle);
            this.vy = (this.multiplier + Math.random() * 0.5) * Math.sin(this.angle);
            this.r = randBetween(8, 12) + 3 * Math.random();
            this.color = colours[Math.floor(Math.random() * colours.length)];
        }

        update(override) {
            this.x += this.vx * override;
            this.y += this.vy * override;
            this.r -= 0.3 * override;
            this.vx *= Math.pow(0.9, override);
            this.vy *= Math.pow(0.9, override);
        }
    }

//  0.9 ^ 5

    function pushBalls(count = 1, x = origin.x, y = origin.y) {
        for (let i = 0; i < count; i++) {
            balls.push(new Ball(x, y));
        }
    }

    function randBetween(min, max) {
        return Math.floor(Math.random() * max) + min;
    }

    let lastTimestamp = performance.now();

    function loop(timestamp) {
        if (!option) return;
        const deltaTime = timestamp - lastTimestamp;
        ctx.fillStyle = "rgba(255, 255, 255, 0)";
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        for (let i = 0; i < balls.length; i++) {
            let b = balls[i];
            if (b.r < 0) continue;
            ctx.fillStyle = b.color;
            ctx.beginPath();
            ctx.arc(b.x, b.y, b.r, 0, Math.PI * 2, false);
            ctx.fill();
            b.update(deltaTime / 16.7);
        }
        if (longPressed) {
            multiplier += 0.2;
        } else if (multiplier >= 0) {
            multiplier -= 0.4;
        }
        removeBall();
        lastTimestamp = timestamp;

        requestAnimationFrame(loop);
    }

    function removeBall() {
        for (let i = 0; i < balls.length; i++) {
            let b = balls[i];
            if (b.x + b.r < 0 || b.x - b.r > width || b.y + b.r < 0 || b.y - b.r > height || b.r < 0) {
                balls.splice(i, 1);
            }
        }
    }

    /**
     *
     * @param {EventTarget|Element} el
     */
    function check(el) {
        let dom = el;
        while (dom != null) {
            if (dom.matches('.ant-popover-inner,.ant-float-btn,button,a')) {
                return false;
            }
            dom = dom.parentElement;
        }
        return true
    }

    return () => {
        document.body.removeChild(canvas);
        removeEventListener()
    }
}

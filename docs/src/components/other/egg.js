const keyboardKey = {
    up: "ArrowUp",
    down: "ArrowDown",
    left: "ArrowLeft",
    right: "ArrowRight",
    keyB: "b",
    keyA: "a",
}
const p = [
    keyboardKey.up,
    keyboardKey.up,
    keyboardKey.down,
    keyboardKey.down,
    keyboardKey.left,
    keyboardKey.right,
    keyboardKey.left,
    keyboardKey.right,
    keyboardKey.keyB,
    keyboardKey.keyA,
]

let nowIndex = 0;
function easterEggTrigger(keyboardEvent) {
    if (keyboardEvent.key === p[nowIndex]) {
        nowIndex ++;
        if (nowIndex >= p.length) {
            nowIndex = 0;
            doSomeThing();
        } else {
            tips()
        }
    } else {
        nowIndex = 0;
    }
}

function doSomeThing () {
    document.documentElement.setAttribute("data-rgb", "true");
    unregister();
}

let boomAudioElement;

function getAudioElement() {
    if (!boomAudioElement) {
        boomAudioElement = document.createElement('audio');
        boomAudioElement.volume = 0.3;
        document.body.appendChild(boomAudioElement);
    }
    return boomAudioElement;
}

function tips() {
    const audio = getAudioElement();
    if (audio.src !== "/audio/boom.wav") {
        audio.src = "/audio/boom.wav";
        audio.load();
    }
    if (!audio.paused) {
        audio.pause();
    }
    audio.currentTime = 0;
    audio.play();
}

function unregister() {
    document.removeEventListener("keydown", easterEggTrigger);
    boomAudioElement.remove();
    boomAudioElement = void 0;
}

export default function setEasterEggTriggered() {
    document.addEventListener("keydown", easterEggTrigger);
    return unregister;
}

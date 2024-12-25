
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
            tips();
        }
    } else {
        nowIndex = 0;
    }
}

let boomAudioElement;

function getAudioElement() {
    if (!boomAudioElement) {
        boomAudioElement = document.createElement('audio');
        boomAudioElement.volume = 0.3;
        boomAudioElement.src = "/audio/boom.wav";
        document.body.appendChild(boomAudioElement);
    }
    return boomAudioElement;
}

function doSomeThing () {
    const audio = document.createElement('audio');
    audio.volume = 0.3;
    audio.src = "/audio/start.mp3";
    document.body.appendChild(audio);
    audio.load();

    audio.currentTime = 0;
    audio.play().catch(e=>console.error(e));

    unregister();
    audio.remove();
}

function tips() {
    const audio = getAudioElement();

    if (!audio.paused) {
        audio.pause();
    }
    audio.currentTime = 0;
    audio.play();
}

function unregister() {
    try {
        document.removeEventListener("keydown", easterEggTrigger);
        boomAudioElement.remove();
        boomAudioElement = void 0;
    } catch (e) {
        // ignore
    }
}

export default function setEasterEggTriggered() {
    document.addEventListener("keydown", easterEggTrigger);
    return unregister;
}

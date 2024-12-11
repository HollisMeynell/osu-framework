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
        }
    } else {
        nowIndex = 0;
    }
}

function doSomeThing () {
    alert("彩蛋触发");
}

export default function setEasterEggTriggered() {
    document.addEventListener("keydown", easterEggTrigger);
    return () => {
        document.removeEventListener("keydown", easterEggTrigger);
    }
}

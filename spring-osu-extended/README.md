# osu extended module

---
## rosu-pp-bind

### how to use

for other usage examples, please refer to the [rosu-pp](https://github.com/MaxOhn/rosu-pp)

- get beatmap

kotlin code:
```kotlin
import kotlin.io.path.Path
import org.spring.osu.extended.rosu.JniBeatmap

// from local file
val beatmap = JniBeatmap(Path("local path to .osu file"))

// from byteArray(utf8 encoded)
val beatmapByByteArray = JniBeatmap(byteArray)

beatmap.use { map ->
    // do something
    map.mode // osu mode
    map.ar
    map.objects
    map.convertInPlace(OsuMode.Taiko) // convert to taiko mode
}
// or use beatmap.close()
beatmap.close()
```
java code:

```java
import org.spring.osu.extended.rosu.JniBeatmap;
import java.nio.file.Path;

// from local file
JniBeatmap beatmap = new JniBeatmap(Path.of("local path to .osu file"));

// from byte[](utf8 encoded)
JniBeatmap beatmapByByteArray = new JniBeatmap(bytes);

// with try-resource
try (beatmap) {
    // do something
    beatmap.getMode(); // osu mode
    beatmap.getAr();
    beatmap.getObjects();
    // ... get field
    beatmap.convertInPlace(OsuMode.Taiko); // convert to taiko mode
}
// or use beatmap.close()
beatmap.close();
```
- calculate difficulty

```kotlin
// create
val difficulty = JniDifficulty(isLazer = false)
// set mode, beatmap must equal to mode 
difficulty.setMode(beatmap.mode)
// create with beatmap, will use beatmap.mode, it safe to calculate by this beatmap
val difficulty = beatmap.createDifficulty()

// withMods `true` ignore mods, `false` modified based on the mods.
difficulty.setCs(5, false)
difficulty.setMods(OsuMod.Hidden, OsuMod.DoubleTime)
// or lazer mods json
difficulty.setMods("""[{"acronym":"DA","settings":{"approach_rate":7.0}},{"acronym":"DT","settings":{"speed_change":1.3}},{"acronym":"HD"}]""")

// calculate
val difficultyAttributes = difficulty.calculate(beatmap)

difficultyAttributes.getStarRating()
difficultyAttributes.getMaxCombo()

when (difficultyAttributes) {
    is OsuDifficultyAttributes -> {
        difficultyAttributes.speed
        difficultyAttributes.aim
        // ... other field
    }
    is TaikoDifficultyAttributes -> {}
    // ...
}

```

- calculate performance point

```kotlin
// create by attributes
val performance = difficultyAttributes.createPerformance()
// create by beatmap
val performance = beatmap.createPerformance.apply {
    setLazer(false)
    setCombo(100)
    setN300(100)
    setMods(/* like difficulty.setMods() */)
}

val result = performance.calculate()
when(result) {
    is OsuPerformanceAttributes -> {
        result.pp
        result.acc
        // ... other field
        // get difficulty attributes
        result.difficulty 
    }
    is TaikoPerformanceAttributes -> {}
    // ...
}
```

### build

compilation environment: java jdk 21, gradle 8.8, rust 1.82.0

version in [build](/build.gradle.kts)

- build for local maven repository

```shell
gradle publishAllToLocal
```
add to your project

```kotlin
repositories {
    mavenLocal()
}
dependencies("org.spring:osu-extended:${version}")
```
or
```xml
<dependencies>
    <dependency>
        <groupId>org.spring</groupId>
        <artifactId>osu-extended</artifactId>
        <version>${version}</version>
    </dependency>
</dependencies>
```

- build for fatJar

```shell
gradle spring-osu-extended:shadowJar
```

copy `spring-osu-extended/build/libs/spring-osu-extended-${version}-all.jar` to your project
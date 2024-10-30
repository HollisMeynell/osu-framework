@file: Suppress("unused")

package org.spring.image

import io.github.humbleui.skija.Image
import io.github.humbleui.skija.ImageInfo
import io.github.humbleui.skija.Surface
import java.util.PriorityQueue
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

object Color {
    const val WHITE: Int = 0xFFFFFFFF.toInt()
    const val BLACK: Int = 0xFF000000.toInt()
    const val MIN_ALPHA_SEARCH_MAX_ITERATIONS = 10
    const val MIN_ALPHA_SEARCH_PRECISION = 1


    fun red(color: Int) = color shr 16 and 0xFF
    fun green(color: Int) = color shr 8 and 0xFF
    fun blue(color: Int) = color and 0xFF
    fun alpha(color: Int) = color ushr 24 and 0xFF
    fun rgb(red: Int, green: Int, blue: Int) = (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
    fun argb(alpha: Int, red: Int, green: Int, blue: Int) = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    fun constrain(amount: Float, low: Float, high: Float): Float {
        return if (amount < low) low
        else if (amount > high) high
        else amount
    }

    fun colorToHSL(color: Int, outHsl: FloatArray) {
        RGBToHSL(red(color), green(color), blue(color), outHsl)
    }

    fun RGBToHSL(r: Int, g: Int, b: Int, outHsl: FloatArray) {
        val rf = r / 255f
        val gf = g / 255f
        val bf = b / 255f

        val max = maxOf(rf, gf, bf)
        val min = minOf(rf, gf, bf)
        val deltaMaxMin = max - min

        var h: Float
        val s: Float
        val l = (max + min) / 2f

        if (max == min) {
            h = 0f
            s = 0f
        } else {
            h = when (max) {
                rf -> ((gf - bf) / deltaMaxMin) % 6f
                gf -> ((bf - rf) / deltaMaxMin) + 2f
                else -> ((rf - gf) / deltaMaxMin) + 4f
            }
            s = deltaMaxMin / (1f - abs(2f * l - 1f))
        }
        h = (h * 60f) % 360f
        if (h < 0) {
            h += 360f
        }
        outHsl[0] = constrain(h, 0f, 360f)
        outHsl[1] = constrain(s, 0f, 1f)
        outHsl[2] = constrain(l, 0f, 1f)
    }

    fun setAlphaComponent(color: Int, alpha: Int): Int {
        require(alpha in 0..255) { "alpha must be between 0 and 255." }
        return (color and 0x00FFFFFF) or (alpha shl 24)
    }

    fun compositeAlpha(foregroundAlpha: Int, backgroundAlpha: Int): Int {
        return 0xFF - (((0xFF - backgroundAlpha) * (0xFF - foregroundAlpha)) / 0xFF)
    }

    fun compositeComponent(
        fgC: Int,
        fgA: Int,
        bgC: Int,
        bgA: Int,
        a: Int,
    ): Int {
        if (a == 0) return 0
        return ((0xFF * fgC * fgA) + (bgC * bgA * (0xFF - fgA))) / (a * 0xFF)
    }

    fun compositeColors(fg: Int, bg: Int): Int {
        val bgAlpha = alpha(bg)
        val fgAlpha = alpha(fg)
        val a = compositeAlpha(fgAlpha, bgAlpha)

        val r = compositeComponent(red(fg), fgAlpha, red(bg), bgAlpha, a)
        val g = compositeComponent(green(fg), fgAlpha, green(bg), bgAlpha, a)
        val b = compositeComponent(blue(fg), fgAlpha, blue(bg), bgAlpha, a)
        return argb(a, r, g, b)
    }

    fun colorToXYZ(color: Int, outXyz: DoubleArray) {
        val r = red(color)
        val g = green(color)
        val b = blue(color)
        var sr = r / 255.0
        sr = if (sr < 0.04045) sr / 12.92 else ((sr + 0.055) / 1.055).pow(2.4)
        var sg = g / 255.0
        sg = if (sg < 0.04045) sg / 12.92 else ((sg + 0.055) / 1.055).pow(2.4)
        var sb = b / 255.0
        sb = if (sb < 0.04045) sb / 12.92 else ((sb + 0.055) / 1.055).pow(2.4)

        outXyz[0] = 100 * (sr * 0.4124 + sg * 0.3576 + sb * 0.1805)
        outXyz[1] = 100 * (sr * 0.2126 + sg * 0.7152 + sb * 0.0722)
        outXyz[2] = 100 * (sr * 0.0193 + sg * 0.1192 + sb * 0.9505)
    }

    fun calculateLuminance(color: Int): Double {
        val result = DoubleArray(3)
        colorToXYZ(color, result)
        return result[1] / 100
    }

    fun calculateContrast(foreground: Int, background: Int): Double {
        require(alpha(background) == 255) { "background can not be translucent: $background" }
        require(alpha(foreground) == 255) { "foreground can not be translucent: $foreground" }

        val luminance1 = calculateLuminance(foreground) + 0.05
        val luminance2 = calculateLuminance(background) + 0.05

        return if (luminance1 > luminance2) {
            luminance1 / luminance2
        } else {
            luminance2 / luminance1
        }
    }

    fun calculateMinimumAlpha(foreground: Int, background: Int, minContrastRatio: Double): Int {
        require(alpha(background) == 255) { "background can not be translucent: $background" }
        var testForeground = setAlphaComponent(foreground, 255)
        var testRatio = calculateContrast(testForeground, background)

        if (testRatio < minContrastRatio) {
            return -1
        }

        var numIterations = 0
        var minAlpha = 0
        var maxAlpha = 255

        while (numIterations <= MIN_ALPHA_SEARCH_MAX_ITERATIONS && (maxAlpha - minAlpha) > MIN_ALPHA_SEARCH_PRECISION) {
            val alpha = (minAlpha + maxAlpha) / 2

            testForeground = setAlphaComponent(foreground, alpha)
            testRatio = calculateContrast(testForeground, background)

            if (testRatio < minContrastRatio) {
                minAlpha = alpha
            } else {
                maxAlpha = alpha
            }
            numIterations++
        }

        return maxAlpha
    }
}

class Target {
    constructor() {
        setTargetDefaultValues(mSaturationTargets)
        setTargetDefaultValues(mLightnessTargets)
        setDefaultWeights()
    }

    constructor(from: Target) {
        System.arraycopy(from.mSaturationTargets, 0, mSaturationTargets, 0, mSaturationTargets.size)
        System.arraycopy(from.mLightnessTargets, 0, mLightnessTargets, 0, mLightnessTargets.size)
        System.arraycopy(from.mWeights, 0, mWeights, 0, mWeights.size)
    }

    val mSaturationTargets = FloatArray(3)
    val mLightnessTargets = FloatArray(3)
    val mWeights = FloatArray(3)
    var mIsExclusive = true

    fun getMinimumSaturation() = mSaturationTargets[INDEX_MIN]
    fun getTargetSaturation() = mSaturationTargets[INDEX_TARGET]
    fun getMaximumSaturation() = mSaturationTargets[INDEX_MAX]

    fun getMinimumLightness() = mLightnessTargets[INDEX_MIN]
    fun getTargetLightness() = mLightnessTargets[INDEX_TARGET]
    fun getMaximumLightness() = mLightnessTargets[INDEX_MAX]

    fun getSaturationWeight() = mWeights[INDEX_WEIGHT_SAT]
    fun getLightnessWeight() = mWeights[INDEX_WEIGHT_LUMA]
    fun getPopulationWeight() = mWeights[INDEX_WEIGHT_POP]

    fun isExclusive() = mIsExclusive

    fun setDefaultWeights() {
        mWeights[INDEX_WEIGHT_SAT] = WEIGHT_SATURATION
        mWeights[INDEX_WEIGHT_LUMA] = WEIGHT_LUMA
        mWeights[INDEX_WEIGHT_POP] = WEIGHT_POPULATION
    }

    fun normalizeWeights() {
        var sum = 0f
        for (weight in mWeights) {
            if (weight > 0) {
                sum += weight
            }
        }
        if (sum != 0f) {
            mWeights.forEachIndexed { i, v ->
                if (v > 0) {
                    mWeights[i] /= sum
                }
            }
        }
    }

    companion object {
        private const val TARGET_DARK_LUMA: Float = 0.26f
        private const val MAX_DARK_LUMA: Float = 0.45f

        private const val MIN_LIGHT_LUMA: Float = 0.55f
        private const val TARGET_LIGHT_LUMA: Float = 0.74f

        private const val MIN_NORMAL_LUMA: Float = 0.3f
        private const val TARGET_NORMAL_LUMA: Float = 0.5f
        private const val MAX_NORMAL_LUMA: Float = 0.7f

        private const val TARGET_MUTED_SATURATION: Float = 0.3f
        private const val MAX_MUTED_SATURATION: Float = 0.4f

        private const val TARGET_VIBRANT_SATURATION: Float = 1f
        private const val MIN_VIBRANT_SATURATION: Float = 0.35f

        private const val WEIGHT_SATURATION: Float = 0.24f
        private const val WEIGHT_LUMA: Float = 0.52f
        private const val WEIGHT_POPULATION: Float = 0.24f

        const val INDEX_MIN: Int = 0
        const val INDEX_TARGET: Int = 1
        const val INDEX_MAX: Int = 2

        const val INDEX_WEIGHT_SAT: Int = 0
        const val INDEX_WEIGHT_LUMA: Int = 1
        const val INDEX_WEIGHT_POP: Int = 2

        val LIGHT_VIBRANT = Target()
        val VIBRANT = Target()
        val DARK_VIBRANT = Target()
        val LIGHT_MUTED = Target()
        val MUTED = Target()
        val DARK_MUTED = Target()

        init {
            setDefaultLightLightnessValues(LIGHT_VIBRANT)
            setDefaultVibrantSaturationValues(LIGHT_VIBRANT)
            setDefaultNormalLightnessValues(VIBRANT)
            setDefaultVibrantSaturationValues(VIBRANT)
            setDefaultDarkLightnessValues(DARK_VIBRANT)
            setDefaultVibrantSaturationValues(DARK_VIBRANT)
            setDefaultLightLightnessValues(LIGHT_MUTED)
            setDefaultMutedSaturationValues(LIGHT_MUTED)
            setDefaultNormalLightnessValues(MUTED)
            setDefaultMutedSaturationValues(MUTED)
            setDefaultDarkLightnessValues(DARK_MUTED)
            setDefaultMutedSaturationValues(DARK_MUTED)
        }

        private fun setTargetDefaultValues(values: FloatArray) {
            values[INDEX_MIN] = 0f
            values[INDEX_TARGET] = 0.5f
            values[INDEX_MAX] = 1f
        }

        private fun setDefaultDarkLightnessValues(target: Target) {
            target.mLightnessTargets[INDEX_TARGET] = TARGET_DARK_LUMA
            target.mLightnessTargets[INDEX_MAX] = MAX_DARK_LUMA
        }

        private fun setDefaultNormalLightnessValues(target: Target) {
            target.mLightnessTargets[INDEX_MIN] = MIN_NORMAL_LUMA
            target.mLightnessTargets[INDEX_TARGET] = TARGET_NORMAL_LUMA
            target.mLightnessTargets[INDEX_MAX] = MAX_NORMAL_LUMA
        }

        private fun setDefaultLightLightnessValues(target: Target) {
            target.mLightnessTargets[INDEX_MIN] = MIN_LIGHT_LUMA
            target.mLightnessTargets[INDEX_TARGET] = TARGET_LIGHT_LUMA
        }

        private fun setDefaultVibrantSaturationValues(target: Target) {
            target.mSaturationTargets[INDEX_MIN] = MIN_VIBRANT_SATURATION
            target.mSaturationTargets[INDEX_TARGET] = TARGET_VIBRANT_SATURATION
        }

        private fun setDefaultMutedSaturationValues(target: Target) {
            target.mSaturationTargets[INDEX_TARGET] = TARGET_MUTED_SATURATION
            target.mSaturationTargets[INDEX_MAX] = MAX_MUTED_SATURATION
        }
    }
}

interface ColorFilter {
    fun isAllowed(rgb: Int, hsl: FloatArray): Boolean

    companion object {
        val DEFAULT_FILTER = object : ColorFilter {
            val BLACK_MAX_LIGHTNESS = 0.05f
            val WHITE_MIN_LIGHTNESS = 0.95f

            override fun isAllowed(rgb: Int, hsl: FloatArray): Boolean {
                return false
            }

            fun isBlack(hslColor: FloatArray): Boolean = hslColor[2] <= BLACK_MAX_LIGHTNESS

            fun isWhite(hslColor: FloatArray): Boolean = hslColor[2] >= WHITE_MIN_LIGHTNESS

            fun isNearRedILine(hslColor: FloatArray): Boolean =
                hslColor[0] >= 10f && hslColor[0] <= 37f && hslColor[1] <= 0.82f

        }
    }
}

class ColorSwatch(val mRgb: Int, val mPopulation: Int) {
    private val mRed = Color.red(mRgb)
    private val mGreen = Color.green(mRgb)
    private val mBlue = Color.blue(mRgb)

    private var mGeneratedTextColors = false
    private var mTitleTextColor = 0
    private var mBodyTextColor = 0
    private lateinit var mHsl: FloatArray

    fun getRgb() = mRgb
    fun getHsl(): FloatArray {
        if (::mHsl.isInitialized.not()) {
            mHsl = FloatArray(3)
        }
        Color.RGBToHSL(mRed, mGreen, mBlue, mHsl)
        return mHsl
    }

    fun getPopulation() = mPopulation

    fun getTitleTextColor(): Int {
        ensureTextColorsGenerated()
        return mTitleTextColor
    }

    fun getBodyTextColor(): Int {
        ensureTextColorsGenerated()
        return mBodyTextColor
    }

    fun ensureTextColorsGenerated() {
        if (mGeneratedTextColors) return
        val lightBodyAlpha = Color.calculateMinimumAlpha(Color.WHITE, mRgb, MIN_CONTRAST_BODY_TEXT)
        val lightTitleAlpha = Color.calculateMinimumAlpha(Color.WHITE, mRgb, MIN_CONTRAST_TITLE_TEXT)
        if (lightBodyAlpha != -1 && lightTitleAlpha != -1) {
            mBodyTextColor = Color.setAlphaComponent(Color.WHITE, lightBodyAlpha)
            mTitleTextColor = Color.setAlphaComponent(Color.WHITE, lightTitleAlpha)
            mGeneratedTextColors = true
            return
        }

        val darkBodyAlpha = Color.calculateMinimumAlpha(Color.BLACK, mRgb, MIN_CONTRAST_BODY_TEXT)
        val darkTitleAlpha = Color.calculateMinimumAlpha(Color.BLACK, mRgb, MIN_CONTRAST_TITLE_TEXT)
        if (darkBodyAlpha != -1 && darkTitleAlpha != -1) {
            mBodyTextColor = Color.setAlphaComponent(Color.BLACK, darkBodyAlpha)
            mTitleTextColor = Color.setAlphaComponent(Color.BLACK, darkTitleAlpha)
            mGeneratedTextColors = true
            return
        }

        mBodyTextColor = if (lightBodyAlpha != -1) {
            Color.setAlphaComponent(Color.WHITE, lightBodyAlpha)
        } else {
            Color.setAlphaComponent(Color.BLACK, darkBodyAlpha)
        }
        mTitleTextColor = if (lightTitleAlpha != -1) {
            Color.setAlphaComponent(Color.WHITE, lightTitleAlpha)
        } else {
            Color.setAlphaComponent(Color.BLACK, darkTitleAlpha)
        }
        mGeneratedTextColors = true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColorSwatch) return false

        return mRgb == other.mRgb && mPopulation == other.mPopulation
    }

    override fun hashCode(): Int {
        return 31 * mRgb + mPopulation
    }

    override fun toString(): String {
        return "Swatch: RGB: #${Integer.toHexString(mRgb)} Population: $mPopulation"
    }

    companion object {
        private const val MIN_CONTRAST_BODY_TEXT = 4.5
        private const val MIN_CONTRAST_TITLE_TEXT = 3.0
    }
}

class ColorCutQuantizer {
    private val mColors: IntArray
    private var mHistogram: IntArray
    private val mTempHsl = FloatArray(3)
    private var mQuantizedColors: List<ColorSwatch>
    private var mFilters: Array<ColorFilter>

    constructor(pixels: IntArray, maxColors: Int, filters: Array<ColorFilter>) {
        mFilters = filters
        mHistogram = IntArray(1 shl (QUANTIZE_WORD_WIDTH * 3))
        for (i in pixels.indices) {
            val quantizedColor = quantizeFromRgb888(pixels[i])
            pixels[i] = quantizedColor
            mHistogram[quantizedColor]++
        }

        var distinctColorCount = 0
        for (color in mHistogram.indices) {
            if (mHistogram[color] > 0 && shouldIgnoreColor(color)) {
                mHistogram[color] = 0
            }
            if (mHistogram[color] > 0) {
                distinctColorCount++
            }
        }

        mColors = IntArray(distinctColorCount)
        for (color in mHistogram) {
            if (color > 0) {
                mColors[distinctColorCount] = color
                distinctColorCount++
            }
        }

        if (distinctColorCount <= maxColors) {
            mQuantizedColors = ArrayList(mColors.size)
            for (color in mColors) {
                mQuantizedColors += ColorSwatch(approximateToRgb888(color), mHistogram[color])
            }

        } else {
            mQuantizedColors = quantizePixels(maxColors)
        }
    }

    fun getQuantizedColors() = mQuantizedColors

    private class Vbox(
        val mLowerIndex: Int,
        var mUpperIndex: Int,
        val p: ColorCutQuantizer,
    ) {
        var mMinRed: Int = Int.MAX_VALUE
        var mMaxRed: Int = Int.MIN_VALUE
        var mMinGreen: Int = Int.MAX_VALUE
        var mMaxGreen: Int = Int.MIN_VALUE
        var mMinBlue: Int = Int.MAX_VALUE
        var mMaxBlue: Int = Int.MIN_VALUE
        var mPopulation = 0

        init {
            fitBox()
        }

        fun getVolume(): Int = (mMaxRed - mMinRed + 1) * (mMaxGreen - mMinGreen + 1) * (mMaxBlue - mMinBlue + 1)

        fun canSplit(): Boolean = getColorCount() > 1

        fun getColorCount(): Int = mUpperIndex - mLowerIndex + 1

        fun fitBox() {
            val colors = p.mColors
            val hist = p.mHistogram

            var minRed = Int.MAX_VALUE
            var minGreen = Int.MAX_VALUE
            var minBlue = Int.MAX_VALUE

            var maxRed = Int.MIN_VALUE
            var maxGreen = Int.MIN_VALUE
            var maxBlue = Int.MIN_VALUE

            var count = 0

            for (i in mLowerIndex..mUpperIndex) {
                val color = colors[i]
                count += hist[color]

                val r = quantizedRed(color)
                val g = quantizedGreen(color)
                val b = quantizedBlue(color)

                if (r > maxRed) maxRed = r
                if (r < minRed) minRed = r

                if (g > maxGreen) maxGreen = g
                if (g < minGreen) minGreen = g

                if (b > maxBlue) maxBlue = b
                if (b < minBlue) minBlue = b
            }
            mMinRed = minRed
            mMaxRed = maxRed
            mMinGreen = minGreen
            mMaxGreen = maxGreen
            mMinBlue = minBlue
            mMaxBlue = maxBlue
            mPopulation = count
        }

        fun splitBox(): Vbox {
            if (canSplit().not()) {
                throw IllegalStateException("Can not split a box with only 1 color")
            }
            val splitPoint = findSplitPoint()
            val newBox = Vbox(splitPoint + 1, mUpperIndex, p)
            mUpperIndex = splitPoint
            fitBox()
            return newBox
        }

        fun getLongestColorDimension(): Int {
            val redLength = mMaxRed - mMinRed
            val greenLength = mMaxGreen - mMinGreen
            val blueLength = mMaxBlue - mMinBlue

            return if (redLength >= greenLength && redLength >= blueLength) {
                COMPONENT_RED
            } else if (greenLength >= redLength && greenLength >= blueLength) {
                COMPONENT_GREEN
            } else {
                COMPONENT_BLUE
            }
        }

        fun findSplitPoint(): Int {
            val longestDimension = getLongestColorDimension()
            val colors = p.mColors
            val hist = p.mHistogram

            modifySignificantOctet(colors, longestDimension, mLowerIndex, mUpperIndex)
            colors.sort(mLowerIndex, mUpperIndex + 1)
            modifySignificantOctet(colors, longestDimension, mLowerIndex, mUpperIndex)

            val midPoint = mPopulation / 2
            var count = 0
            for (i in mLowerIndex..mUpperIndex) {
                count += hist[colors[i]]
                if (count >= midPoint) {
                    return min(mUpperIndex - 1, i)
                }
            }
            return mLowerIndex
        }

        fun getAverageColor(): ColorSwatch {
            var redSum = 0
            var greenSum = 0
            var blueSum = 0
            var totalPopulation = 0

            for (i in mLowerIndex..mUpperIndex) {
                val color = p.mColors[i]
                val colorPopulation = p.mHistogram[color]

                totalPopulation += colorPopulation
                redSum += colorPopulation * quantizedRed(color)
                greenSum += colorPopulation * quantizedGreen(color)
                blueSum += colorPopulation * quantizedBlue(color)
            }

            val redMean = round(1.0 * redSum / totalPopulation).toInt()
            val greenMean = round(1.0 * greenSum / totalPopulation).toInt()
            val blueMean = round(1.0 * blueSum / totalPopulation).toInt()

            return ColorSwatch(
                approximateToRgb888(redMean, greenMean, blueMean),
                totalPopulation,
            )
        }
    }

    fun quantizePixels(maxColors: Int): List<ColorSwatch> {
        val pq = PriorityQueue<Vbox>(maxColors, Comparator<Vbox> { lhs, rhs ->
            rhs.getVolume() - lhs.getVolume()
        })

        pq.offer(Vbox(0, mColors.size - 1, this))
        splitBoxes(pq, maxColors)
        return generateAverageColors(pq)
    }

    private fun splitBoxes(queue: PriorityQueue<Vbox>, maxSize: Int) {
        while (queue.size < maxSize) {
            val vbox = queue.poll() ?: return
            if (vbox.canSplit()) {
                queue.offer(vbox.splitBox())
                queue.offer(vbox)
            } else {
                return
            }
        }
    }

    private fun generateAverageColors(vboxes: Collection<Vbox>): List<ColorSwatch> {
        val colors = ArrayList<ColorSwatch>(vboxes.size)
        for (vbox in vboxes) {
            val color = vbox.getAverageColor()
            if (shouldIgnoreColor(color).not()) {
                colors.add(color)
            }
        }
        return colors
    }

    private fun shouldIgnoreColor(color565: Int): Boolean {
        val rgb = approximateToRgb888(color565)
        Color.colorToHSL(rgb, mTempHsl)
        return shouldIgnoreColor(rgb, mTempHsl)
    }

    private fun shouldIgnoreColor(color: ColorSwatch): Boolean {
        return shouldIgnoreColor(color.getRgb(), color.getHsl())
    }

    private fun shouldIgnoreColor(rgb: Int, hsl: FloatArray): Boolean {
        if (mFilters.isEmpty()) return false
        for (filter in mFilters) {
            if (filter.isAllowed(rgb, hsl).not()) {
                return true
            }

        }
        return false
    }

    companion object {
        const val COMPONENT_RED = -3
        const val COMPONENT_GREEN = -2
        const val COMPONENT_BLUE = -1


        const val QUANTIZE_WORD_WIDTH = 5
        const val QUANTIZE_WORD_MASK = (1 shl QUANTIZE_WORD_WIDTH) - 1

        fun quantizeFromRgb888(color: Int): Int {
            val r = modifyWordWidth(Color.red(color), 8, QUANTIZE_WORD_WIDTH)
            val g = modifyWordWidth(Color.green(color), 8, QUANTIZE_WORD_WIDTH)
            val b = modifyWordWidth(Color.blue(color), 8, QUANTIZE_WORD_WIDTH)
            return (r shl (QUANTIZE_WORD_WIDTH + QUANTIZE_WORD_WIDTH)) or (g shl QUANTIZE_WORD_WIDTH) or b
        }

        fun approximateToRgb888(r: Int, g: Int, b: Int) = Color.rgb(
            modifyWordWidth(r, QUANTIZE_WORD_WIDTH, 8),
            modifyWordWidth(g, QUANTIZE_WORD_WIDTH, 8),
            modifyWordWidth(b, QUANTIZE_WORD_WIDTH, 8),
        )

        fun approximateToRgb888(color: Int) = approximateToRgb888(
            quantizedRed(color),
            quantizedGreen(color),
            quantizedBlue(color),
        )

        fun quantizedRed(color: Int) = (color shr (QUANTIZE_WORD_WIDTH + QUANTIZE_WORD_WIDTH)) and QUANTIZE_WORD_MASK

        fun quantizedGreen(color: Int) = (color shr QUANTIZE_WORD_WIDTH) and QUANTIZE_WORD_MASK

        fun quantizedBlue(color: Int) = color and QUANTIZE_WORD_MASK

        fun modifyWordWidth(value: Int, currentWidth: Int, targetWidth: Int): Int {
            val newValue = if (targetWidth > currentWidth) {
                value shl (targetWidth - currentWidth)
            } else {
                value ushr (currentWidth - targetWidth)
            }
            return newValue and ((1 shl targetWidth) - 1)
        }

        fun modifySignificantOctet(
            a: IntArray,
            dimension: Int,
            lower: Int,
            upper: Int,
        ) {
            when (dimension) {
                COMPONENT_RED -> {
                    return
                }

                COMPONENT_GREEN -> {
                    for (i in lower..upper) {
                        val c = a[i]
                        a[i] = quantizedGreen(c) shl (QUANTIZE_WORD_WIDTH + QUANTIZE_WORD_WIDTH) or
                                quantizedRed(c) shl QUANTIZE_WORD_WIDTH or
                                quantizedBlue(c)
                    }
                }

                COMPONENT_BLUE -> {
                    for (i in lower..upper) {
                        val c = a[i]
                        a[i] = quantizedBlue(c) shl (QUANTIZE_WORD_WIDTH + QUANTIZE_WORD_WIDTH) or
                                quantizedGreen(c) shl QUANTIZE_WORD_WIDTH or
                                quantizedRed(c)
                    }
                }
            }
        }
    }
}

interface Bitmap {
    fun getWidth(): Int
    fun getHeight(): Int
    fun getPixels(pixes: IntArray, offset: Int, stride: Int, x: Int, y: Int, width: Int, height: Int)
    fun scaled(width: Int, height: Int): Bitmap

    companion object {
        @JvmStatic
        fun createScaledBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
            return bitmap.scaled(width, height)
        }
    }
}

class SkijaMap(val image: io.github.humbleui.skija.Bitmap) : Bitmap {
    override fun getWidth() = image.width

    override fun getHeight() = image.height

    override fun getPixels(
        pixes: IntArray,
        offset: Int,
        stride: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ) {
        for (dx in 0 until width) {
            for (dy in 0 until height) {
                val index = offset + dx + dy * stride
                pixes[index] = image.getColor(x + dx, y + dy)
            }
        }
    }

    override fun scaled(width: Int, height: Int): Bitmap {
        val result = Surface.makeRaster(ImageInfo.makeN32Premul(width, height)).use {
            val canvas = it.canvas
            canvas.scale(width.toFloat() / image.width, height.toFloat() / image.height)
            canvas.drawImage(Image.makeRasterFromBitmap(image), 0f, 0f)
            it.makeImageSnapshot()
        }
        return SkijaMap(io.github.humbleui.skija.Bitmap.makeFromImage(result))
    }
}

class Palette(
    val mSwatches: List<ColorSwatch>,
    val mTargets: List<Target>,
) {
    val mSelectedSwatches = HashMap<Target, ColorSwatch?>()
    val mUsedColors = SparseBooleanArray()
    val mDominantSwatch: ColorSwatch? = findDominantSwatch()

    fun getSwatches() = mSwatches

    fun getTargets() = mTargets

    fun getVibrantColor(defaultColor: Int = 0) = getColorForTarget(Target.VIBRANT, defaultColor)

    fun getLightVibrantColor(defaultColor: Int = 0) = getColorForTarget(Target.LIGHT_VIBRANT, defaultColor)

    fun getDarkVibrantColor(defaultColor: Int = 0) = getColorForTarget(Target.DARK_VIBRANT, defaultColor)

    fun getMutedColor(defaultColor: Int = 0) = getColorForTarget(Target.MUTED, defaultColor)

    fun getLightMutedColor(defaultColor: Int = 0) = getColorForTarget(Target.LIGHT_MUTED, defaultColor)

    fun getDarkMutedColor(defaultColor: Int = 0) = getColorForTarget(Target.DARK_MUTED, defaultColor)

    fun getSwatchForTarget(target: Target) = mSelectedSwatches[target]

    fun getColorForTarget(target: Target, defaultColor: Int) = mSelectedSwatches[target]?.getRgb() ?: defaultColor

    fun getDominantSwatch() = mDominantSwatch

    fun getDominantColor(defaultColor: Int) = mDominantSwatch?.getRgb() ?: defaultColor

    fun generate() {
        mTargets.forEach { target ->
            target.normalizeWeights()
            mSelectedSwatches[target] = generateScoredTarget(target)
        }
        mUsedColors.clear()
    }

    fun generateScoredTarget(target: Target): ColorSwatch? {
        val maxScoreSwatch = getMaxScoredSwatchForTarget(target)
        if (maxScoreSwatch != null && target.isExclusive()) {
            mUsedColors.add(maxScoreSwatch.getRgb(), true)
        }
        return maxScoreSwatch
    }

    fun getMaxScoredSwatchForTarget(target: Target): ColorSwatch? {
        var maxScore = 0f
        var maxSwatch: ColorSwatch? = null
        mSwatches.forEach { swatch ->
            if (shouldBeScoredForTarget(swatch, target)) {
                val score = generateScore(swatch, target)
                if (maxSwatch == null || score > maxScore) {
                    maxSwatch = swatch
                    maxScore = score
                }
            }
        }
        return maxSwatch
    }

    fun shouldBeScoredForTarget(swatch: ColorSwatch, target: Target): Boolean {
        val hsl = swatch.getHsl()
        return hsl[1] >= target.getMinimumSaturation() &&
                hsl[1] <= target.getMaximumSaturation() &&
                hsl[2] >= target.getMinimumLightness() &&
                hsl[2] <= target.getMaximumLightness() &&
                mUsedColors.get(swatch.getRgb()).not()
    }

    fun generateScore(swatch: ColorSwatch, target: Target) : Float {
        val hsl = swatch.getHsl()

        var saturationScore = 0f
        var luminanceScore = 0f
        var populationScore = 0f

        val maxPopulation = mDominantSwatch?.getPopulation() ?: 1

        if (target.getSaturationWeight() > 0) {
            saturationScore = target.getSaturationWeight() * (1 - abs(hsl[1] - target.getTargetSaturation()))
        }
        if (target.getLightnessWeight() > 0) {
            luminanceScore = target.getLightnessWeight() * (1 - abs(hsl[2] - target.getTargetLightness()))
        }
        if (target.getPopulationWeight() > 0) {
            populationScore = target.getPopulationWeight() * (swatch.getPopulation() / maxPopulation)
        }
        return saturationScore + luminanceScore + populationScore
    }

    fun findDominantSwatch(): ColorSwatch? {
        var maxPop = Int.MIN_VALUE
        var maxSwatch: ColorSwatch? = null
        mSwatches.forEach { swatch ->
            if (swatch.getPopulation() > maxPop) {
                maxSwatch = swatch
                maxPop = swatch.getPopulation()
            }
        }
        return maxSwatch
    }

    class Builder(bitmap: Bitmap) {
        var mSwatches: List<ColorSwatch>? = null
        var mBitmap: Bitmap? = null
        var mRegion: Rect? = null
        val mTargets = arrayListOf<Target>()

        var mMaxColors = DEFAULT_CALCULATE_NUMBER_COLORS
        var mResizeArea = DEFAULT_RESIZE_BITMAP_AREA
        var mResizeMaxDimension = -1

        val mFilters = arrayListOf<ColorFilter>()

        init {
            mBitmap = bitmap
            mFilters.add(ColorFilter.DEFAULT_FILTER)

            mTargets += Target.LIGHT_VIBRANT
            mTargets += Target.VIBRANT
            mTargets += Target.DARK_VIBRANT
            mTargets += Target.LIGHT_MUTED
            mTargets += Target.MUTED
            mTargets += Target.DARK_MUTED

        }

        fun setMaximumColorCount(maxColors: Int) {
            mMaxColors = maxColors
        }

        fun resizeBitmapArea(area: Int) {
            mResizeArea = area
            mResizeMaxDimension = -1
        }

        fun addFilter(filter: ColorFilter) {
            mFilters += filter
        }

        fun clearFilters() {
            mFilters.clear()
        }

        fun setRegion(left: Int, top: Int, right: Int, bottom: Int) {
            if (mBitmap == null) {
                return
            }
            if (mRegion == null) {
                mRegion = Rect(0, 0, mBitmap!!.getWidth(), mBitmap!!.getHeight())
            }
            val r = mRegion!!.intersect(left, top, right, bottom)
            if (r.not()) throw IllegalArgumentException("The given region must intersect with " + "the Bitmap's dimensions.")
        }

        fun clearRegion() {
            mRegion = null
        }

        fun addTarget(target: Target) {
            if (mTargets.contains(target).not()) {
                mTargets += target
            }
        }

        fun clearTargets() {
            mTargets.clear()
        }

        fun generate(): Palette {
            val bitmap = scaleBitmapDown(mBitmap!!)
            val region = mRegion
            if (bitmap != mBitmap && region != null) {
                val scale = bitmap.getWidth() / mBitmap!!.getWidth()
                region.left = (region.left * scale).toInt()
                region.top = (region.top * scale).toInt()
                region.right = min((region.right * scale).toInt(), bitmap.getWidth())
                region.bottom = min((region.bottom * scale).toInt(), bitmap.getHeight())
            }
            val filters = Array<ColorFilter>(mFilters.size) { mFilters[it] }
            val quantize = ColorCutQuantizer(
                getPixelsFromBitmap(bitmap),
                mMaxColors,
                filters,
            )
            val swatches = quantize.getQuantizedColors()
            val palette = Palette(swatches, mTargets)
            palette.generate()
            return palette
        }

        fun getPixelsFromBitmap(bitmap: Bitmap): IntArray {
            val bitmapWidth = bitmap.getWidth()
            val bitmapHeight = bitmap.getHeight()
            val pixels = IntArray(bitmapWidth * bitmapHeight)
            bitmap.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight)

            if (mRegion != null) {
                val region = mRegion!!
                val regionWidth = region.width
                val regionHeight = region.height
                val subsetPixels = IntArray(regionWidth * regionHeight)
                for (row in 0 until regionHeight) {
                    System.arraycopy(
                        pixels,
                        ((row + region.top) * bitmapWidth) + region.left,
                        subsetPixels,
                        row * regionWidth,
                        regionWidth
                    )
                }
                return subsetPixels
            } else {
                return pixels
            }
        }

        fun scaleBitmapDown(bitmap: Bitmap): Bitmap {
            var scaleRatio = -1.0
            if (mResizeArea > 0) {
                val bitmapArea = bitmap.getWidth() / bitmap.getHeight()
                if (bitmapArea > mResizeArea) {
                    scaleRatio = sqrt(1.0 * mResizeArea / bitmapArea)
                }
            } else if (mResizeMaxDimension > 0) {
                val maxDimension = max(bitmap.getWidth(), bitmap.getHeight())
                if (maxDimension > mResizeMaxDimension) {
                    scaleRatio = 1.0 * mResizeMaxDimension / maxDimension
                }
            }

            if (scaleRatio <= 0) {
                return bitmap
            }
            return Bitmap.createScaledBitmap(
                bitmap,
                ceil(bitmap.getWidth() * scaleRatio).toInt(),
                ceil(bitmap.getHeight() * scaleRatio).toInt(),
            )
        }
    }

    class Rect(
        var left: Int,
        var top: Int,
        var right: Int,
        var bottom: Int,
    ) {
        val width: Int
            get() = right - left
        val height: Int
            get() = bottom - top

        fun intersect(l: Int, t: Int, r: Int, b: Int): Boolean {
            if (left < l || top < t || right > r || bottom > b) {
                if (left < l) left = l
                if (top < t) top = t
                if (right > r) right = r
                if (bottom > b) bottom = b
                return true
            }
            return false
        }
    }

    class SparseBooleanArray{
        private var array = IntArray(10)
        private var booleanArray = BooleanArray(10)
        private var size = 0
        fun clear() {
            size = 0
        }

        private fun search(key: Int) : Int {
            for (i in 0 until size) {
                if (array[i] == key) {
                    return i
                }
            }
            return -1
        }

        fun get(key:Int, default: Boolean = false) : Boolean {
            val index = search(key)
            return if (index != -1) {
                booleanArray[index]
            } else {
                default
            }
        }

        fun add(key:Int, b: Boolean) {
            val index = search(key)
            if (index >= 0) {
                booleanArray[index] = b
                return
            }
            if (size + 1 >= array.size) {
                array = array.copyOf(array.size * 2)
                booleanArray = booleanArray.copyOf(booleanArray.size * 2)
            }
            array[size] = key
            booleanArray[size] = b
            size++
        }
    }

    companion object {
        const val DEFAULT_CALCULATE_NUMBER_COLORS = 16
        const val DEFAULT_RESIZE_BITMAP_AREA = 112 * 112
        const val MIN_CONTRAST_TITLE_TEXT = 3f
        const val MIN_CONTRAST_BODY_TEXT = 4.5f

        fun from(bitmap: Bitmap) = Builder(bitmap)
    }
}
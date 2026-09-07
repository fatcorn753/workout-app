package com.tatu.workout.pushup

/**
 * 顔のバウンディングボックスの高さ(px)の推移から腕立て伏せの往復をカウントする。
 *
 * スマホを床に置きインカメラを自分に向ける想定。腕を曲げて顔がカメラに近づくと
 * バウンディングボックスは大きくなり、腕を伸ばすと小さくなる。この上下動を
 * 直近数秒の動的レンジに対する相対位置として捉え、ヒステリシス付きの状態遷移で
 * 1往復＝1repをカウントする。絶対距離は使わないが、閾値を固定値にしないことで
 * 体格差や設置距離のばらつきを吸収する。
 */
class RepCounter(
    private val windowMillis: Long = 2500L,
    private val smoothingAlpha: Float = 0.4f,
    private val enterBottomRatio: Float = 0.35f,
    private val enterTopRatio: Float = 0.65f,
    /** このレンジ幅(px)未満は静止・ノイズとみなし、状態遷移を起こさない。 */
    private val minRangePx: Float = 18f,
) {
    private enum class State { TOP, BOTTOM }

    private data class Sample(val timestampMillis: Long, val value: Float)

    private val window = ArrayDeque<Sample>()
    private var smoothed: Float? = null
    private var state = State.TOP

    var repCount = 0
        private set

    /** 直近の正規化位置(0=底, 1=頂点)。UI のプログレス表示用。範囲が定まらない間は null。 */
    var normalizedPosition: Float? = null
        private set

    fun reset() {
        window.clear()
        smoothed = null
        state = State.TOP
        repCount = 0
        normalizedPosition = null
    }

    /** @return このフレームで rep が確定したら true */
    fun onFaceHeight(heightPx: Float, timestampMillis: Long): Boolean {
        val prevSmoothed = smoothed
        val s = if (prevSmoothed == null) heightPx else prevSmoothed + smoothingAlpha * (heightPx - prevSmoothed)
        smoothed = s

        window.addLast(Sample(timestampMillis, s))
        while (window.isNotEmpty() && timestampMillis - window.first().timestampMillis > windowMillis) {
            window.removeFirst()
        }

        val minV = window.minOf { it.value }
        val maxV = window.maxOf { it.value }
        val range = maxV - minV
        if (range < minRangePx) {
            normalizedPosition = null
            return false
        }

        val normalized = ((s - minV) / range).coerceIn(0f, 1f)
        normalizedPosition = normalized

        return when (state) {
            State.TOP -> {
                if (normalized <= enterBottomRatio) state = State.BOTTOM
                false
            }
            State.BOTTOM -> {
                if (normalized >= enterTopRatio) {
                    state = State.TOP
                    repCount++
                    true
                } else {
                    false
                }
            }
        }
    }
}

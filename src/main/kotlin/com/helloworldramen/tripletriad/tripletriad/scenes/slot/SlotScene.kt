package com.helloworldramen.tripletriad.tripletriad.scenes.slot

import com.helloworldramen.tripletriad.tripletriad.scenes.card.PlayerCardScene
import godot.Area2D
import godot.ColorRect
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterSignal
import godot.core.Color
import godot.extensions.getNodeAs
import godot.global.GD
import godot.signals.signal

@RegisterClass
class SlotScene: Area2D() {

	@RegisterSignal
	val signalSlotEntered by signal<SlotScene>("slotScene")

	@RegisterSignal
	val signalSlotExited by signal<SlotScene>("slotScene")

	private val colorRect: ColorRect by lazy { getNodeAs("ColorRect")!! }

	@RegisterFunction
	override fun _ready() {
		areaEntered.connect(this, this::onAreaEntered)
		areaExited.connect(this, this::onAreaExited)
		unhighlight()
	}

	@RegisterFunction
	fun highlight() {
		colorRect.color = Color(0.88, 0.86, 0.8)
	}

	fun unhighlight() {
		colorRect.color = Color(0.76, 0.73, 0.64)
	}

	@RegisterFunction
	fun onAreaEntered(otherArea2D: Area2D) {
		if (otherArea2D is PlayerCardScene) {
			signalSlotEntered.emit(this)
		}
	}

	@RegisterFunction
	fun onAreaExited(otherArea2D: Area2D) {
		if (otherArea2D is PlayerCardScene) {
			signalSlotExited.emit(this)
		}
	}
}

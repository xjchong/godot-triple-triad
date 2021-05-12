package com.helloworldramen.tripletriad.tripletriad.scenes.slot

import com.helloworldramen.tripletriad.tripletriad.scenes.card.PlayerCardScene
import godot.Area2D
import godot.CollisionShape2D
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
	val signalCardEntered by signal<SlotScene>("slotScene")

	@RegisterSignal
	val signalCardExited by signal<SlotScene>("slotScene")

	private val colorRect: ColorRect by lazy { getNodeAs("ColorRect")!! }

	@RegisterFunction
	override fun _ready() {
		areaEntered.connect(this, this::onAreaEntered)
		areaExited.connect(this, this::onAreaExited)
		unhighlight()
	}

	@RegisterFunction
	fun highlight() {
		colorRect.color = Color.darkgray
	}

	fun unhighlight() {
		colorRect.color = Color.dimgray
	}

	@RegisterFunction
	fun onAreaEntered(otherArea2D: Area2D) {
		if (otherArea2D is PlayerCardScene) {
			GD.print("slot on area entered")
			signalCardEntered.emit(this)
		} else {
			GD.print("slot on area entered (dud)")
		}
	}

	@RegisterFunction
	fun onAreaExited(otherArea2D: Area2D) {
		if (otherArea2D is PlayerCardScene) {
			GD.print("slot on area exited")
			signalCardExited.emit(this)
		} else {
			GD.print("slot on area exited (dud)")
		}
	}
}

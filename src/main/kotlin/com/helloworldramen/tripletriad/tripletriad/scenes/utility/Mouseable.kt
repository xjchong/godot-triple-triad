package com.helloworldramen.tripletriad.tripletriad.scenes.utility

import godot.Control
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterSignal
import godot.signals.signal

@RegisterClass
class Mouseable: Control() {

	@RegisterSignal
	val signalMouseIn by signal<Control>("parent")

	@RegisterSignal
	val signalMouseOut by signal<Control>("parent")

	var isMouseIn: Boolean = false

	@RegisterFunction
	override fun _process(delta: Double) {
		(getParent() as? Control)?.let { parent ->
			if (getGlobalRect().hasPoint(getGlobalMousePosition())) {
				if (!isMouseIn) {
					isMouseIn = true
					signalMouseIn.emit(parent)
				}
			} else if (isMouseIn) {
				isMouseIn = false
				signalMouseOut.emit(parent)
			}
		}
	}
}

package com.helloworldramen.tripletriad.tripletriad.scenes.game_toast

import godot.AnimationPlayer
import godot.ColorRect
import godot.Label
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.core.Color
import godot.extensions.getNodeAs
import godot.global.GD

@RegisterClass
class GameToastScene: ColorRect() {

	private val toastLabel: Label by lazy { getNodeAs("ToastLabel")!! }
	private val animationPlayer: AnimationPlayer by lazy { getNodeAs("AnimationPlayer")!! }

	@RegisterFunction
	override fun _ready() {
		this.color = Color.transparent
		toastLabel.text = ""
	}

	@RegisterFunction
	fun toastBlueTurn() {
		animationPlayer.play("blue_turn")
	}

	@RegisterFunction
	fun toastRedTurn() {
		animationPlayer.play("red_turn")
	}
}

package com.helloworldramen.tripletriad.tripletriad.scenes.card

import godot.ColorRect
import godot.Node
import godot.Spatial
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.core.Color
import godot.core.NodePath
import godot.global.GD

@RegisterClass
class PlayerCard: Spatial() {

	private val backgroundColorRect: ColorRect by lazy {
		getNode(NodePath("Container/BackgroundColorRect")) as ColorRect
	}

	@RegisterFunction
	override fun _ready() {
		GD.print("Hello world!")
//		val firstChild = getChildren().front() as? Node
//		GD.print(firstChild?.getPath())
		GD.print("${backgroundColorRect.color} before")
		backgroundColorRect.color = Color(100, 0, 0)
		GD.print("${backgroundColorRect.color} after")
	}
}

package com.helloworldramen.tripletriad.tripletriad.scenes.card

import com.helloworldramen.tripletriad.tripletriad.models.Card
import com.helloworldramen.tripletriad.tripletriad.models.CardRarity
import com.helloworldramen.tripletriad.tripletriad.models.CardType
import com.helloworldramen.tripletriad.tripletriad.models.PlayerCard
import com.helloworldramen.tripletriad.tripletriad.scenes.utility.Mouseable
import godot.*
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterSignal
import godot.core.Color
import godot.core.NodePath
import godot.core.Vector2
import godot.extensions.getNodeAs
import godot.global.GD
import godot.signals.signal

@RegisterClass
class PlayerCardScene: Area2D() {

	@RegisterSignal
	val signalPlayerCardEntered by signal<PlayerCardScene>("cardScene")

	@RegisterSignal
	val signalPlayerCardExited by signal<PlayerCardScene>("cardScene")

	private val container: AspectRatioContainer by lazy { getNodeAs("Container")!! }
	private val colorRect: ColorRect by lazy { getNodeAs(colorRectPath())!! }
	private val rarityLabel: Label by lazy { getNodeAs(colorRectPath("RarityLabel"))!! }
	private val typeLabel: Label by lazy { getNodeAs(colorRectPath("TypeLabel"))!! }
	private val nameLabel: Label by lazy { getNodeAs(colorRectPath("NameLabel"))!! }
	private val northLabel: Label by lazy { getNodeAs(colorRectPath("NorthLabel"))!! }
	private val eastLabel: Label by lazy { getNodeAs(colorRectPath("EastLabel"))!! }
	private val southLabel: Label by lazy { getNodeAs(colorRectPath("SouthLabel"))!! }
	private val westLabel: Label by lazy { getNodeAs(colorRectPath("WestLabel"))!! }
	private val mouseable: Mouseable by lazy { getNodeAs("Container/Mouseable")!! }
	private val tween: Tween by lazy { getNodeAs("Tween")!! }

	private fun colorRectPath(path: String = ""): String {
		return "Container/MarginContainer/ColorRect${if (path.isEmpty()) "" else "/$path"}"
	}

	@RegisterFunction
	override fun _ready() {
		mouseable.connect("mouse_in", this, "on_mouse_entered")
		mouseable.connect("mouse_out", this, "on_mouse_exited")
	}

	@RegisterFunction
	fun onMouseEntered(parent: Control) {
		signalPlayerCardEntered.emit(this)
	}

	@RegisterFunction
	fun onMouseExited(parent: Control) {
		signalPlayerCardExited.emit(this)
	}

	@RegisterFunction
	fun highlight() {
		tween.interpolateProperty(this, NodePath("scale"), null, Vector2(1.1, 1.1), duration = 0.05)
		tween.start()
	}

	@RegisterFunction
	fun unhighlight() {
		tween.interpolateProperty(this, NodePath("scale"), null, Vector2(1, 1), duration = 0.05)
		tween.start()
	}


	fun bind(playerCard: PlayerCard?) {
		if (playerCard?.card == Card.UNKNOWN) {
			bindUnknown()
		} else {
			rarityLabel.text = when(playerCard?.card?.rarity) {
				CardRarity.Common -> "*"
				CardRarity.Uncommon -> "**"
				CardRarity.Rare -> "***"
				CardRarity.Epic -> "****"
				CardRarity.Legendary -> "*****"
				else -> "?????"
			}

			typeLabel.text = when(playerCard?.card?.type) {
				CardType.Beastman -> "B"
				CardType.Garland -> "G"
				CardType.Primal -> "P"
				CardType.Scions -> "S"
				else -> ""
			}

			nameLabel.text = playerCard?.card?.name?.take(7) ?: "???????"

			northLabel.text = playerCard?.n().getValueString()
			eastLabel.text = playerCard?.e().getValueString()
			southLabel.text = playerCard?.s().getValueString()
			westLabel.text = playerCard?.w().getValueString()
		}

		colorRect.color = when {
			playerCard?.isPlayable == false -> Color.gray
			playerCard?.playerId == 0 -> Color.cadetblue
			playerCard?.playerId == 1 -> Color.mediumvioletred
			else -> Color.black
		}

		if (playerCard == null) container.hide() else container.show()
	}

	private fun bindUnknown() {
		rarityLabel.text = ""
		typeLabel.text = ""
		nameLabel.text = ""
		northLabel.text = ""
		eastLabel.text = ""
		southLabel.text = ""
		westLabel.text = ""
	}

	private fun Int?.getValueString(): String {
		return when {
			this == null -> "?"
			this >= 10 -> "A"
			this <= 0 -> "1"
			else -> this.toString()
		}
	}
}
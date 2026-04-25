package org.evolution.model.card

import org.evolution.model.trait.*

class TraitCard(
    id: Int,
    val traitType: TraitType
) : Card(id) {

    override fun play() {
        // Логика розыгрыша карты реализована в Game.playCard()
    }

    fun createNewTrait(): Trait {
        return when (traitType) {
            TraitType.PREDATOR -> PredatorTrait(id)
            TraitType.FAT -> FatTrait(id)
            TraitType.AQUATIC -> AquaticTrait(id)
            TraitType.BURROWING -> BurrowingTrait(id)
            TraitType.CAMOUFLAGE -> CamouflageTrait(id)
            TraitType.SHARP_VISION -> SharpVisionTrait(id)
            TraitType.POISONOUS -> PoisonousTrait(id)
            TraitType.RUNNING -> RunningTrait(id)
            TraitType.SYMBIOSIS -> SymbiosisTrait(id)
            TraitType.PIRACY -> PiracyTrait(id)
            TraitType.COOPERATION -> CooperationTrait(id)
            TraitType.COMMUNICATION -> CommunicationTrait(id)
            TraitType.SCAVENGER -> ScavengerTrait(id)
            TraitType.LARGE -> LargeTrait(id)
            TraitType.TRAMPLING -> TramplingTrait(id)
            TraitType.TAIL_LOSS -> TailLossTrait(id)
            TraitType.HIBERNATION -> HibernationTrait(id)
            TraitType.MIMICRY -> MimicryTrait(id)
            TraitType.PARASITE -> ParasiteTrait(id)
        }
    }
}
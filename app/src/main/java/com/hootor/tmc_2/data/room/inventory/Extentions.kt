package com.hootor.tmc_2.data.room.inventory

import com.hootor.tmc_2.data.room.inventory.entities.TMCDbInventoryItems
import com.hootor.tmc_2.domain.tmc.TMCTree

fun List<TMCDbInventoryItems>.toTMCTree() : TMCTree {

    val roots: List<TMCTree> = this.filter {
        it.tmsParent.isEmpty()
    }.map {
        TMCTree(uuid = it.tms, title = it.tmsTitle, children = mutableListOf(), expandable = true)
    }
    roots.forEach { tmcTree ->
        val ch: List<TMCDbInventoryItems> = this.filter { itemDb->
            itemDb.tmsParent == tmcTree.uuid
        }
    }
    return TMCTree("root", "root", children = roots)
}
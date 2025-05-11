package com.typewritermc.quest

import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.TypewriterCommand
import com.typewritermc.engine.paper.command.dsl.*
import com.typewritermc.engine.paper.utils.msg

@TypewriterCommand
fun CommandTree.questCommand() = literal("quest") {
    withPermission("typewriter.quest")
    literal("track") {
        withPermission("typewriter.quest.track")
        entry<QuestEntry>("quest") { quest ->
            executePlayerOrTarget { target ->
                target.trackQuest(quest().ref())
                sender.msg("你正在追踪<blue>${quest().display(target)}</blue>。")
            }
        }
    }

    literal("untrack") {
        withPermission("typewriter.quest.untrack")
        executePlayerOrTarget { target ->
            target.unTrackQuest()
            sender.msg("你不再追踪任何任务。")
        }
    }
}
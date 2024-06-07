package oppgavestyring.filter

import oppgavestyring.oppgave.NavIdent
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

class OppgaveFilter(id: EntityID<Long>) : LongEntity(id) {
    companion object: LongEntityClass<OppgaveFilter>(OppgaveFilterTable)

    var tittel by OppgaveFilterTable.tittel
    var beskrivelse by OppgaveFilterTable.beskrivelse
    var filter by OppgaveFilterTable.filter
    val opprettetTid by OppgaveFilterTable.opprettetTid
    var opprettetAv by OppgaveFilterTable.opprettetAv
    val tildelt by FilterTildelt referrersOn FilterTildeltTable.oppgave

}

class FilterTildelt(id: EntityID<Long>) : LongEntity(id) {
    companion object: LongEntityClass<FilterTildelt>(FilterTildeltTable)

    private var _navIdent by FilterTildeltTable.navIdent
    var navIdent: NavIdent
        set(value) { _navIdent = value.asString() }
        get() = NavIdent(_navIdent)
    var hovedFilter by FilterTildeltTable.hovedfilter
    val oppgave by FilterTildeltTable.oppgave

}

object OppgaveFilterTable: LongIdTable("OPPGAVE_FILTER") {
    val tittel = varchar("TITTEL", 50)
    val beskrivelse = varchar("BESKRIVELSE", 255)
    val filter = text("FILTER_JSON", )
    val opprettetTid = datetime("OPPRETTET_TID").defaultExpression(CurrentDateTime)
    val opprettetAv = varchar("OPPRETTET_AV", 7)
}

object FilterTildeltTable: LongIdTable("FILTER_TILDELT") {
    val oppgave = reference("OPPGAVE_FILTER_ID", OppgaveFilterTable)
    val navIdent = varchar("NAVIDENT", 7)
    val hovedfilter = bool("HOVEDFILTER").default(false)
}


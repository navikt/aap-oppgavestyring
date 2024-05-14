package oppgavestyring.oppgave.db

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.behandlingsflyt.dto.Behandlingstype
import oppgavestyring.behandlingsflyt.dto.Oppgavestatus
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp


class Oppgave(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Oppgave>(OppgaveTabell)

    var behandlingsreferanse by OppgaveTabell.behandlingsreferanse
    var behandlingstype by OppgaveTabell.behandlingstype
    var status by OppgaveTabell.status
    var avklaringsbehovtype by OppgaveTabell.avklaringbehovtype
    var gjelderverdi by OppgaveTabell.gjelderverdi
    private val _opprettet by OppgaveTabell.opprettet
    val opprettet: LocalDateTime
        get() = _opprettet.toLocalDateTime(TimeZone.currentSystemDefault())

    val utførere by Utfører referrersOn UtførerTabell.oppgave
    val tildelt by Tildelt backReferencedOn TildeltTabell.oppgave
}

class Utfører(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Utfører>(UtførerTabell)

    var ident by UtførerTabell.ident
    var tidsstempel by UtførerTabell.tidsstempel
    var oppgave by Oppgave referencedOn UtførerTabell.oppgave
}

class Tildelt(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Tildelt>(TildeltTabell)

    var ident by TildeltTabell.ident
    var tidsstempel by TildeltTabell.tidsstempel
    var oppgave by Oppgave referencedOn TildeltTabell.oppgave
}

private object OppgaveTabell: IntIdTable("OPPGAVE") {
    val behandlingsreferanse = uuid("BEHANDLINGSREFERANSE")
    val behandlingstype = enumerationByName("BEHANDLINGSTYPE", 50, Behandlingstype::class)
    val status = enumerationByName("STATUS", 50, Oppgavestatus::class)
    val avklaringbehovtype = enumerationByName("AVKLARINGBEHOVTYPE", 50, Avklaringsbehovtype::class)
    val gjelderverdi = varchar("GJELDERVERDI", 50).default("")
    val opprettet = timestamp("OPPRETTET").defaultExpression(CurrentTimestamp)
}

private object UtførerTabell: IntIdTable("UTFORER") {
    val ident = varchar("IDENT", 50)
    val tidsstempel = timestamp("TIDSSTEMPEL").defaultExpression(CurrentTimestamp)

    val oppgave = reference("OPPGAVE_ID", OppgaveTabell)
}

private object TildeltTabell: IntIdTable("TILDELT") {
    val ident = varchar("IDENT", 50)
    val tidsstempel = timestamp("TIDSSTEMPEL").defaultExpression(CurrentTimestamp)

    val oppgave = reference("OPPGAVE_ID", OppgaveTabell)
}


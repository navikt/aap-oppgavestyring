package oppgavestyring.oppgave.db


import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovstatus
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.behandlingsflyt.dto.Behandlingstype
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.timestamp

class Oppgave(id: EntityID<Long>): LongEntity(id) {
    companion object : LongEntityClass<Oppgave>(OppgaveTabell)

    var behandlingsreferanse by OppgaveTabell.behandlingsreferanse
    var behandlingstype by OppgaveTabell.behandlingstype
    var status by OppgaveTabell.status
    var avklaringsbehovtype by OppgaveTabell.avklaringbehovtype
    var gjelderverdi by OppgaveTabell.gjelderverdi
    var avklaringsbehovOpprettetTidspunkt by OppgaveTabell.avklaringsbehovOpprettetTidspunkt
    var behandlingOpprettetTidspunkt by OppgaveTabell.behandlingOpprettetTidspunkt
    var personnummer by OppgaveTabell.personnummer

    val utførere by Utfører referrersOn UtførerTabell.oppgave
    val tildelt by Tildelt optionalBackReferencedOn TildeltTabell.oppgave
}

class Utfører(id: EntityID<Long>): LongEntity(id) {
    companion object : LongEntityClass<Utfører>(UtførerTabell)

    var ident by UtførerTabell.ident
    var tidsstempel by UtførerTabell.tidsstempel
    var oppgave by Oppgave referencedOn UtførerTabell.oppgave
}

class Tildelt(id: EntityID<Long>): LongEntity(id) {
    companion object : LongEntityClass<Tildelt>(TildeltTabell)

    var ident by TildeltTabell.ident
    var tidsstempel by TildeltTabell.tidsstempel
    var oppgave by Oppgave referencedOn TildeltTabell.oppgave
}

object OppgaveTabell: LongIdTable("OPPGAVE") {
    val behandlingsreferanse = varchar("BEHANDLINGSREFERANSE", 50)
    val behandlingstype = enumerationByName("BEHANDLINGSTYPE", 50, Behandlingstype::class)
    val status = enumerationByName("STATUS", 50, Avklaringsbehovstatus::class)
    val avklaringbehovtype = enumerationByName("AVKLARINGBEHOVTYPE", 50, Avklaringsbehovtype::class)
    val gjelderverdi = varchar("GJELDERVERDI", 50).default("")
    val personnummer = varchar("PERSONNUMMER", 11)
    val avklaringsbehovOpprettetTidspunkt = datetime("AVKLARINGSBEHOV_OPPRETTET_TIDSPUNKT")
    val behandlingOpprettetTidspunkt = datetime("BEHANDLING_OPPRETTET_TIDSPUNKT")
}

object UtførerTabell: LongIdTable("UTFORER") {
    val ident = varchar("IDENT", 50)
    val tidsstempel = timestamp("TIDSSTEMPEL").defaultExpression(CurrentTimestamp)

    val oppgave = reference("OPPGAVE_ID", OppgaveTabell)
}

object TildeltTabell: LongIdTable("TILDELT") {
    val ident = varchar("IDENT", 50)
    val tidsstempel = timestamp("TIDSSTEMPEL").defaultExpression(CurrentTimestamp)

    val oppgave = reference("OPPGAVE_ID", OppgaveTabell)
}

package oppgavestyring.config

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import oppgavestyring.behandlingsflyt.BehandlingsflytAdapter
import oppgavestyring.config.db.DatabaseConfiguration
import oppgavestyring.config.db.DatabaseManager
import oppgavestyring.oppgave.OppgaveService
import org.koin.dsl.module

val koinModule = module {

    single { OppgaveService() }
    single { BehandlingsflytAdapter(get()) }
    single { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }
    single(createdAtStart = true) { DatabaseManager(DatabaseConfiguration()) }

}
# Docsafey

Servizio back-end REST per la gestione delle **richieste di conservazione digitale** e dei documenti ad esse associati, conservazione a norma di documenti, tracciabilità delle richieste, gestione del ciclo di vita di una pratica dalla ricezione al completamento.

## Indice

- [Stack tecnologico](#stack-tecnologico)
- [Requisiti](#requisiti)
- [Avvio del progetto](#avvio-del-progetto)
- [Struttura del progetto](#struttura-del-progetto)
- [API](#api)
- [Configurazione](#configurazione)
- [Testing](#testing)
- [Evoluzione: validazione asincrona tramite sistema esterno](#evoluzione-validazione-asincrona-tramite-sistema-esterno)
- [Scalabilità: gestire un milione di richieste](#scalabilità-gestire-un-milione-di-richieste)

---

## Stack tecnologico

| Componente | Tecnologia |
|---|---|
| Linguaggio | Java 21 |
| Framework | Spring Boot 4.0.7 (Web MVC, Data JPA, Validation, Security) |
| Database | PostgreSQL 17 |
| Documentazione API | springdoc-openapi (Swagger UI) |
| Build | Maven (wrapper incluso, `mvnw`) |
| Containerizzazione | Docker, Docker Compose |
| Testing | JUnit 5, Spring Boot Test starters |
| Utility | Lombok |

## Requisiti

Per l'esecuzione **locale** (senza container per l'app):

- JDK 21+
- Maven 3.9+ 
- Docker + Docker Compose v2 (usato per avviare solo il database)

Per l'esecuzione **completamente containerizzata**:

- Docker + Docker Compose v2 (nessun requisito Java/Maven sulla macchina host)

## Avvio del progetto

Docsafey usa la dipendenza `spring-boot-docker-compose`: quando l'applicazione viene avviata da IDE o da Maven (non da container), Spring Boot rileva automaticamente il file `compose.yaml` nella root del progetto, avvia il servizio Postgres e configura da solo il `DataSource` puntando al container appena creato — senza bisogno di impostare manualmente `SPRING_DATASOURCE_*`. Il container viene fermato automaticamente allo shutdown dell'app.

### Opzione A — sviluppo locale (consigliata durante lo sviluppo)

```bash
./mvnw spring-boot:run
```

Cosa succede:
1. Spring Boot legge `compose.yaml` e avvia il container Postgres (con healthcheck).
2. Attende che il DB sia pronto e configura automaticamente la connessione.
3. Avvia l'applicazione su `http://localhost:8080`.

Documentazione delle APIs disponibile su `http://localhost:8080/swagger-ui.html`.

### Opzione B — stack completamente containerizzato

```bash
docker compose --profile full up --build
```

Per un deploy similar-production in cui anche l'app gira in un container.

## Struttura del progetto

```
src/main/java/com/intesi/docsafey/
├── config/            # configurazione cross-cutting (SecurityConfig)
├── controller/         # livello REST, mapping HTTP <-> service
├── dto/                # oggetti di scambio verso l'esterno (request/response), separati dalle entity
│   ├── documento/
│   └── richiestaCons/
├── entity/              # modello di persistenza JPA
│   ├── documento/
│   └── richiestaCons/
├── exception/           # eccezioni di dominio + gestione centralizzata
│   ├── documento/
│   └── richiestaCons/
├── mapper/              # conversione entity <-> DTO
├── repository/          # Spring Data JPA repository + Specification per query dinamiche
│   └── specification/
├── service/             # logica di business e orchestrazione delle transizioni di stato
└── validation/          # validazione custom (Bean Validation)
    ├── annotation/
    └── validator/
```

L'architettura segue la classica separazione a livelli (**Controller → Service → Repository**) con DTO dedicati in ingresso/uscita (mai esposta direttamente l'entity JPA), un layer `mapper` per il disaccoppiamento, e un `GlobalExceptionHandler` centralizzato per la gestione degli errori.

## API

Base path: `/v1/conservazione`

| Metodo | Path | Descrizione |
|---|---|---|
| `GET` | `/{id}` | Recupera una richiesta di conservazione per ID |
| `GET` | `?producerId=&status=` | Ricerca paginata (filtri opzionali su producer e stato) |
| `POST` | `/` | Crea una nuova richiesta di conservazione |
| `PUT` | `/validate/{id}` | Registra l'esito della validazione (validata/rifiutata) |
| `PUT` | `/complete/{id}` | Segna la richiesta come completata |

La ricerca supporta paginazione standard Spring Data (`Pageable`, default `size=20`) tramite `@PageableDefault`.

Tutte le APIs sono documentate utilizzando swagger-ui e openAPI, la documentazione viene esposta sul path default,all'avvio dell'applicazione: http://localhost:8080/swagger-ui/index.html

## Configurazione

| Variabile | Descrizione | Dove viene usata |
|---|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL Postgres | profilo `docker` (container) |
| `SPRING_DATASOURCE_USERNAME` | utente DB | profilo `docker` |
| `SPRING_DATASOURCE_PASSWORD` | password DB | profilo `docker` |
| `SPRING_PROFILES_ACTIVE` | profilo Spring attivo (`docker`) | container app |
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | credenziali del container Postgres | `compose.prod.yaml`, da `.env` |

In sviluppo locale (Opzione A) **non serve valorizzare nulla di tutto ciò**: il Docker Compose support di Spring Boot configura da solo la connessione al Postgres avviato da `compose.yaml`.

## Testing

```bash
./mvnw test
```

Sono presenti test per controller (`RIchiestaConsControllerTest`) e service (`RichiestaConsServiceTest`).
---

## Evoluzione: validazione asincrona tramite sistema esterno

Ipotesi: la validazione di richieste e documenti non avviene più internamente/sincronamente (`PUT /validate/{id}` come azione manuale immediata), ma viene demandata a un **sistema esterno** che riceve la richiesta, effettua i controlli (es. verifica hash, formato, firma, conformità) e restituisce l'esito in modo asincrono.

### Flusso proposto

Considerando che la comunicazione con il sistema esterno viene effettuato in modo asincrono, è possibile utilizzare un message broker come **RabbitMQ**. Prendendo una request dal Client, essa verrà elaborata dall'applicativo Docsafey. Dopo aver elaborato e salvato la richiesta, verrà pubblicato un evento che verrà poi consumato dal sistema di validazione esterno. Infine, dopo che il servizio esterno abbia terminato le adeguate validazioni, ritornerà l'esito su una coda, specifica per i risultati, in modo tale che l'applicativo aggiorni internamente la richiesta.

1. Client Request -> Ricezione Docsafey
2. Elaborazione e salvataggio richiesta
3. Docsafey -> RabbitMQ
4. RabbitMQ -> Sistema di validazione esterno
5. Sistema di validazione esterno -> RabbitMQ
6. RabbitMQ -> Docsafey

### Nuovi stati della richiesta

L'enum `RichiestaStatus` si estende per distinguere **l'esito di business** (validato/rifiutato) da **un problema tecnico o di comunicazione**, informazione che oggi non esiste perché la validazione è sincrona e "atomica" dal punto di vista del chiamante:

```java
public enum RichiestaStatus {
    RECEIVED,             // creata, non ancora inviata a validazione
    VALIDATION_PENDING,    // NUOVO — evento pubblicato, in attesa di esito dal sistema esterno
    VALIDATED,
    REJECTED,
    VALIDATION_FAILED,     // NUOVO — timeout o errore tecnico nella comunicazione col validatore
    COMPLETED
}
```

Dato che una richesta di conservazione comprende più documenti, può avere senso introdurre uno stato anche a livello del singolo documento, poichè più documenti della stessa richiesta potrebbero avere esiti diversi. Tuttavia, viene sottointesa una logica adatta del sistema esterno.

```java
public enum DocumentoStatus {
    PENDING,
    VALID,
    INVALID
}
```
### Modifiche al modello dati

- **`documenti`**: aggiungere `status` (`DocumentoStatus`), `validation_details` (testo/JSON con il motivo di un eventuale esito negativo), `validated_at`.
- **`richieste_conservazione`**: aggiungere `correlation_id` (UUID) per correlare la richiesta pubblicata su RabbitMQ con l'esito ricevuto in modo asincrono, aggiungere `validation_requested_at`.
---

## Scalabilità: gestire un milione di richieste

### Indici da introdurre

| Indice | Motivazione |
|---|---|
| `(producer_id, status)` su `richieste_conservazione` | copre il filtro combinato usato da `GET /v1/conservazione?producerId=&status=`; oggi il vincolo `UNIQUE(producer_id, external_id)` crea già un indice ma con colonna leading diversa, non ottimale per filtrare per stato |
| `created_at` (eventualmente combinato con `id` come tiebreaker) | necessario per l'ordinamento e per la paginazione per keyset (vedi sotto) |
| `request_id` su `documenti` | **Postgres non indicizza automaticamente le foreign key** (a differenza della PK) — senza questo indice, ogni join o `WHERE request_id = ?` su una tabella da milioni di righe degenera in una scansione sequenziale |
| `hash` su `documenti` | usato da `@UniqueHash` per verificare la deduplica; senza indice, ogni creazione di documento costerebbe una scansione completa della tabella |

Da valutare inoltre se `hash` debba diventare un **vincolo di unicità a livello DB** (non solo validazione applicativa): un validator Bean Validation esegue tipicamente un controllo "check-then-act" (query di esistenza, poi insert) che è soggetto a *race condition* sotto concorrenza — due richieste concorrenti con lo stesso hash potrebbero superare entrambe il controllo prima che una delle due venga persistita. Un vincolo `UNIQUE` a livello DB, con gestione della relativa `DataIntegrityViolationException` nel service, chiude questa finestra.

### Paginazione

L'implementazione attuale (`Pageable` di Spring Data) usa **paginazione offset-based** (`OFFSET`/`LIMIT`), adeguata per le prime pagine ma che degrada linearmente in profondità: per restituire la pagina 5.000 il database deve comunque attraversare e scartare le 100.000 righe precedenti.

Su un dataset di un milione di righe, per gli endpoint ad alto traffico conviene affiancare:
- Ordinamento stabile: `created_at DESC, id DESC`.
- Query risultante: `WHERE (created_at, id) < (:afterCreatedAt, :afterId) ORDER BY created_at DESC, id DESC LIMIT :size`, che sfrutta pienamente l'indice su `(created_at, id)` con un costo costante indipendente dalla profondità.

### Come individuare una query lenta

1. Abilitare l'estensione `pg_stat_statements` su Postgres per avere, per ogni query normalizzata, conteggio esecuzioni, tempo totale e medio.
2. Impostare `log_min_duration_statement` (es. 200ms) per loggare automaticamente ogni query più lenta della soglia.
3. Lato applicativo, abilitare temporaneamente `hibernate.generate_statistics` in ambiente non di produzione per contare quante query genera effettivamente una singola richiesta HTTP 

### Come investigare un degrado prestazionale

Un approccio sistematico, dal generale al particolare:
1. **Database**: `pg_stat_activity` per connessioni bloccate o query lunghe in corso, `pg_locks` per contention, verificare se l'autovacuum sta tenendo il passo (tabelle molto scritte con troppi dead tuple degradano nel tempo anche query che usano correttamente gli indici).
2. **Applicativo**: query N+1 non ancora individuate, cache mancante su letture ripetute, batch size non adeguato per gli insert massivi.
---

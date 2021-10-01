# Злой Бот
Привет, это репозиторий лучшего бота в телеге!:heart_eyes:  
Бот многофункционален и может работать в нескольких чатах одновременно!:yum:  
Можете добавить [@zloychatbot](https://t.me/zloychatbot) в свой чат или склонировать репозиторий для создания своего!:star_struck:

## Возможности Злого Бота
На данный момент бот имеет в своем арсенале следующие возможности:
### Капча :astonished:
*Для работы боту необходимы права администратора в чате!*

В бот встроена капча для новых юзеров в чате и приветственное сообщение.  
Также проверяется есть ли юзер в списке [CAS](https://cas.chat/). :cop:
### Команды :monocle_face:
* */statistic* - статистика сообщений юзера в чате;
* */top10* - топ 10 спамеров в чате;
* */me* - бот будет писать за тебя;
* */ktozloy* - находит злого юзера в чате;
* */f* - отдать честь;
* */resolve* или */r* - решает сложные вопросы;
* */currency* или */cur* - перевод валюты;
* */time* - текущее время;
* */sed* - работа со строкой с помощью sed;
* */switch_gender* - сменить пол юзера;
* */continue* - продолжить текст с помощью [GPT-3](https://ru.wikipedia.org/wiki/GPT-3);
* */cat* - сгенерировать котика;

*Для работы следующих команд нужно быть администратором чата и боту необходимы права администратора:*
* */block_stickerpack* - заблокировать стикерпак в чате;
* */unblock_stickerpack* - разблокировать стикерпак в чате;
### Тригеры :sunglasses:
У бота есть обширный список слов тригеров на которые он реагирует и отвечает.
### Webhook :yum:
Также бот умеет в [webhook](https://core.telegram.org/bots/api#setwebhook):
>```properties
>   telegram.bot.webhook.url = ""
>   telegram.bot.webhook.port = 8080
>```
## Запуск своего бота :nerd_face:
Можно запустить бота напрямую на [Heroku](https://heroku.com/) или самостоятельно!
### Запуск на Heroku :dancer:
Нажимаем на кнопку и переходим к настройкам  
[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)
### Настройка Heroku :mechanic:
#### Устанавливаем Config Vars
Ключ API полученный от [Fixer.io](https://fixer.io/)
>```
>   FIXER_API_KEY
>```
Ключ API полученный от [LocationiQ.com](https://locationiq.com/)
>```
>   LOCATIONIQ_API_KEY
>```
Токен телеграм бота полученный от [BotFather](https://t.me/BotFather)
>```
>   TELEGRAM_BOT_TOKEN
>```
Адрес вашего приложения Heroku: «https:// ```app-name``` .herokuapp.com/».
>```
>   TELEGRAM_BOT_WEBHOOK_URL
>```
### Самостоятельный запуск :man_technologist:
### Загрузка :chart_with_upwards_trend:
Устанавливаем [JDK](https://www.oracle.com/java/technologies/javase-downloads.html) если ее нет, минимальная версия: 8.  
Клонируем репозиторий с [GitHub](https://github.com/Djaler/evil-bot).
### Настройка :suspect:
Для запуска необходимо заполнить следующие поля в *application.properties* или задать переменные окружения:
#### Токен телеграм бота :rage1:
Токен телеграм бота полученный от [BotFather](https://t.me/BotFather)
>application.properties:
>```properties
>   telegram.bot.token =
>```
>Окружение:
>```bash
>   export TELEGRAM_BOT_TOKEN=
>```
#### Ключ сервиса для конвертации валют :goberserk:
Ключ API полученный от [Fixer.io](https://fixer.io/)
>application.properties:
>```properties
>   fixer.api.key =
>```
>Окружение:
>```bash
>   export FIXER_API_KEY=
>```
#### Ключ сервиса для определения времени :finnadie:
Ключ API полученный от [LocationiQ.com](https://locationiq.com/)
>application.properties:
>```properties
>   locationiq.api.key =
>```
>Окружение:
>```bash
>   export LOCATIONIQ_API_KEY=
>```
#### Настройка базы данных :feelsgood:
Настройка базы данных, используется СУБД [PostgreSQL](https://www.postgresql.org/)
>application.properties:
>```properties
>   spring.datasource.url =
>   spring.datasource.username =
>   spring.datasource.password =
>```
>Окружение:
>```bash
>   export SPRING_DATASOURCE_URL=
>   export SPRING_DATASOURCE_USERNAME=
>   export SPRING_DATASOURCE_PASSWORD=
>```
>
> Например:
>
> Пример **docker-compose** для создания базы данных
> ```dockerfile
>   version: '3.8'
>   services:
>       evil_bot_db:
>           container_name: evil_bot_db_container
>           image: postgres:13-alpine
>           restart: always
>           environment:
>               POSTGRES_USER: evil_bot
>               POSTGRES_PASSWORD: evil_bot_pass
>               POSTGRES_DB: evil_bot_db
>           ports:
>               - "5432:5432"
>           volumes:
>               - pg_data:/var/lib/postgresql/data/
>   volumes:
>       pg_data:
> ```
>Соответственное заполнение полей для этого примера:
> ```properties
>   spring.datasource.url = jdbc:postgresql://localhost:5432/evil_bot_db
>   spring.datasource.username = evil_bot
>   spring.datasource.password = evil_bot_pass
>```
> или
>```bash
>   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/evil_bot_db
>   export SPRING_DATASOURCE_USERNAME=evil_bot
>   export SPRING_DATASOURCE_PASSWORD=evil_bot_pass
>```
### Запуск :rocket:
После всех настроек приступим к запуску!  
#### Тестовый запуск :snail:
> для Unix
> ```bash
>   ./gradlew bootRun 
> ```
> для Windows
> ```powershell
>   ./gradlew.bat bootRun
> ```
#### Если все прошло успешно, то можно приступить к сборке! :yum:
> для Unix
> ```bash
>   ./gradlew bootJar
> ```
> для Windows
> ```powershell
>   ./gradlew.bat bootJar
> ```
#### А теперь запускаем в ./build/libs! :man_technologist:
> для Unix
> ```bash
>   cd ./build/libs
>   java -jar evil-bot-1.0-SNAPSHOT.jar
> ```
> для Windows
> ```powershell
>   cd ./build/libs
>   java.exe -jar evil-bot-1.0-SNAPSHOT.jar
> ```

## Лицензия :speech_balloon:
MIT License

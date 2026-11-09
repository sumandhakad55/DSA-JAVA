# Kharcha - Daily Expense Tracker

Track what you spend on Blinkit, Flipkart, Instamart and other apps, see where your money went in any month,
and get a message every night such as:
"Good evening! You spent ₹234 today. Your current balance is ₹10,000. Keep tracking your spending!"

Stack: HTML + CSS + JavaScript (frontend), Spring Boot 3 + JPA (backend), MySQL (database).

## How the payment data gets in
PhonePe and Google Pay do not offer a public API to read personal transactions, so Kharcha supports:
1. Quick manual entry (Add expense button).
2. Paste a bank debit SMS (Expenses page) - amount, shop and PhonePe/Google Pay are detected.
3. Import a bank / PhonePe / Google Pay statement saved as CSV (Expenses page). Duplicates are skipped.
4. Automatic SMS capture on Android: in MacroDroid or Tasker, create a rule "when an SMS containing 'debited'
   arrives, send an HTTP POST to http://YOUR-SERVER:8080/api/expenses/parse-sms with JSON body
   {"text": "<sms text>"}".
Rules (Settings page) decide which app and category a keyword belongs to.

## Folder structure
```
expense-tracker/
  pom.xml
  README.md
  sql/                      schema.sql, data.sql, sample-expenses.sql (optional demo data)
  src/main/java/com/tracker/
    ExpenseTrackerApplication.java
    config/WebConfig.java
    entity/        Expense, Income, AppSetting, MerchantRule
    repository/    ExpenseRepository, IncomeRepository, SettingRepository, RuleRepository
    dto/           ExpenseRequest, IncomeRequest, RuleRequest, SettingsRequest, SmsRequest
    exception/     NotFoundException, BadRequestException, GlobalExceptionHandler
    service/       ExpenseService, IncomeService, SummaryService, SettingsService, RuleService,
                   SmsParserService, CsvImportService
    notification/  NotificationChannel, TelegramChannel, EmailChannel, NotificationService, DailyReportScheduler
    controller/    ExpenseController, IncomeController, SummaryController, SettingsController, NotificationController
    util/TextUtil.java
  src/main/resources/
    application.properties, schema.sql, data.sql
    static/        index.html, style.css, app.js
```

## Run it
1. Install JDK 17, Maven and MySQL 8.
2. Set your MySQL login (or edit application.properties):
   - Windows PowerShell:  `$env:DB_USER="root"; $env:DB_PASSWORD="yourpassword"`
   - Linux / macOS:       `export DB_USER=root DB_PASSWORD=yourpassword`
   The database `expense_tracker` and its tables are created automatically on first start.
3. From the project folder run: `mvn spring-boot:run`
4. Open http://localhost:8080
5. (Optional) load demo data by running sql/sample-expenses.sql in MySQL Workbench.
6. Open Settings and enter your opening balance and monthly budget.

## Telegram nightly notification (free)
1. In Telegram, search for @BotFather, send /newbot, follow the steps, and copy the bot token.
2. Open your new bot and send it any message (for example "hi").
3. Open https://api.telegram.org/bot<YOUR_TOKEN>/getUpdates in a browser and copy the number after `"chat":{"id":`.
4. Set environment variables before starting the app:
   `TELEGRAM_BOT_TOKEN=<token>` and `TELEGRAM_CHAT_ID=<chat id>`
5. Open Settings in Kharcha and press "Send test notification".
6. The time is `tracker.notification.cron` in application.properties. Default is 9:00 PM every night.
   Example for 10:30 PM: `0 30 22 * * *`

## Email notification (optional)
Uncomment the `spring.mail.*` lines in application.properties, set MAIL_USER, MAIL_APP_PASSWORD
(a Gmail app password) and NOTIFY_EMAIL_TO.

## Good to know
- The nightly message is only sent while the app is running at that time. A cloud server (Render, Railway,
  a small VPS) or an always-on PC works best.
- There is no login. Keep it on your own computer or home network. If you put it on the internet,
  add Spring Security first, because anyone with the link could see your spending.

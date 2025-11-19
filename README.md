Bank Card Management System
Система управления банковскими картами с ролевой моделью доступа и безопасными транзакциями.

🚀 Быстрый старт
1. Клонирование репозитория
bash
git clone <url-репозитория>
cd card-management-system
2. Запуск приложения
bash
docker-compose up -d
Приложение будет доступно по адресу: http://localhost:8080

🔐 Тестирование функциональности
1. Регистрация пользователей
Регистрация Администратора:

bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "email": "admin@bank.com",
    "role": "ADMIN"
  }'
Регистрация Пользователя:

bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "user123",
    "email": "user1@bank.com",
    "role": "USER"
  }'
2. Авторизация и получение токенов
Авторизация Администратора:

bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
Ответ:

json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
Авторизация Пользователя:

bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "user123"
  }'
Сохраните полученные токены для следующих запросов.

📋 Тестирование эндпоинтов
Для Администратора (используйте admin token)
Создание карты:

bash
curl -X POST http://localhost:8080/api/cards \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4111111111111234",
    "ownerName": "IVAN IVANOV",
    "expiryDate": "12/25",
    "balance": 1000.00,
    "userId": 2
  }'
Просмотр всех карт:

bash
curl -X GET http://localhost:8080/api/cards \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
Блокировка карты:

bash
curl -X PUT http://localhost:8080/api/cards/1/block \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
Для Пользователя (используйте user token)
Просмотр своих карт:

bash
curl -X GET http://localhost:8080/api/cards/my-cards \
  -H "Authorization: Bearer YOUR_USER_TOKEN"
Перевод между картами:

bash
curl -X POST http://localhost:8080/api/transfers \
  -H "Authorization: Bearer YOUR_USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fromCardId": 1,
    "toCardId": 2,
    "amount": 100.00
  }'
Запрос на блокировку карты:

bash
curl -X POST http://localhost:8080/api/cards/1/block-request \
  -H "Authorization: Bearer YOUR_USER_TOKEN"
📊 Документация API
После запуска приложения документация доступна по адресу:

Swagger UI: http://localhost:8080/swagger-ui.html

OpenAPI спецификация: http://localhost:8080/v3/api-docs

🛠 Технические детали
База данных: PostgreSQL (порт 5432)

Миграции: Liquibase (автоматически применяются при запуске)

Аутентификация: JWT токены

Документация: OpenAPI 3.0

🔧 Остановка приложения
bash
docker-compose down
📞 Поддержка
При возникновении вопросов можете писать в tg - @XALLEN1


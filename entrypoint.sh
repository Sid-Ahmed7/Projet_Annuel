#!/bin/sh
export SPRING_DATASOURCE_USERNAME=$(cat /run/secrets/db_user)
export SPRING_DATASOURCE_PASSWORD=$(cat /run/secrets/db_password)
export SPRING_DATASOURCE_URL="jdbc:postgresql://db:5432/$(cat /run/secrets/db_name)"
export JWT_SECRET=$(cat /run/secrets/jwt_secret)
export TOTP_ENCRYPTION=$(cat /run/secrets/totp_encryption)
export MAIL_PASSWORD=$(cat /run/secrets/mail_password)
export MAIL_USERNAME=$(cat /run/secrets/mail_username)
export STRIPE_SECRET_KEY=$(cat /run/secrets/stripe_secret_key)
export STRIPE_PUBLIC_KEY=$(cat /run/secrets/stripe_public_key)
export STRIPE_WEBHOOK_SECRET=$(cat /run/secrets/stripe_webhook_secret)
export FLYWAY_ADMIN_PASS=$(cat /run/secrets/flyway_admin_pass)
export FLYWAY_SECRET_KEY=$(cat /run/secrets/flyway_secret_key)
export FLYWAY_EMAIL=$(cat /run/secrets/flyway_email)
export FLYWAY_LAST_NAME=$(cat /run/secrets/flyway_last_name)
export FLYWAY_FIRST_NAME=$(cat /run/secrets/flyway_first_name)
exec java -jar /app/app.jar
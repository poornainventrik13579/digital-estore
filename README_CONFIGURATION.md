# Digital E-Store Configuration Guide

## Environment Variables Setup

### Required Environment Variables

For local development, set these environment variables or use `application-local.properties`:

```bash
# Database Configuration
export DB_URL="jdbc:mysql://localhost:3306/digital_estore"
export DB_USERNAME="root"
export DB_PASSWORD="your_mysql_password"

# Email Configuration
export EMAIL_USERNAME="your_email@gmail.com"
export EMAIL_PASSWORD="your_app_password"
export EMAIL_FROM="your_email@gmail.com"

# Stripe Configuration
export STRIPE_API_KEY="sk_test_your_stripe_key"
export STRIPE_WEBHOOK_SECRET="whsec_your_webhook_secret"
```

### Windows PowerShell Setup
```powershell
$env:DB_PASSWORD="your_mysql_password"
$env:EMAIL_USERNAME="your_email@gmail.com"
$env:EMAIL_PASSWORD="your_app_password"
$env:STRIPE_API_KEY="sk_test_your_stripe_key"
$env:STRIPE_WEBHOOK_SECRET="whsec_your_webhook_secret"
```

### Local Development Alternative

1. Copy `application-local.properties.example` to `application-local.properties`
2. Fill in your actual credentials
3. Add `--spring.profiles.active=local` when running

### Maven Flyway Commands

```bash
# Run migrations
mvn flyway:migrate

# Check migration status
mvn flyway:info

# Repair migrations if needed
mvn flyway:repair
```

## Security Notes

- Never commit actual passwords to git
- Use environment variables in production
- `application-local.properties` is git-ignored
- Keep test/development keys separate from production 
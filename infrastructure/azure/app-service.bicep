param location string = resourceGroup().location
param environmentName string = 'trade-settlement'
param appServicePlanSku string = 'B1'
param backendImage string
param sqlJdbcUrl string
param sqlUsername string
@secure()
param sqlPassword string
param frontendOrigin string

resource plan 'Microsoft.Web/serverfarms@2022-03-01' = {
  name: '${environmentName}-plan'
  location: location
  sku: { name: appServicePlanSku, tier: 'Basic' }
  properties: { reserved: true }
}

resource backend 'Microsoft.Web/sites@2022-03-01' = {
  name: '${environmentName}-api'
  location: location
  kind: 'app,linux,container'
  properties: {
    serverFarmId: plan.id
    siteConfig: {
      linuxFxVersion: 'DOCKER|${backendImage}'
      appSettings: [
        { name: 'SPRING_PROFILES_ACTIVE', value: 'azure' }
        { name: 'AZURE_SQL_JDBC_URL', value: sqlJdbcUrl }
        { name: 'AZURE_SQL_USERNAME', value: sqlUsername }
        { name: 'AZURE_SQL_PASSWORD', value: sqlPassword }
        { name: 'CORS_ALLOWED_ORIGINS', value: frontendOrigin }
      ]
      alwaysOn: true
    }
    httpsOnly: true
  }
}

resource frontend 'Microsoft.Web/staticSites@2022-09-01' = {
  name: '${environmentName}-web'
  location: location
  sku: { name: 'Free', tier: 'Free' }
  properties: { stagingEnvironmentPolicy: 'Enabled' }
}

output backendUrl string = 'https://${backend.properties.defaultHostName}'
output frontendName string = frontend.name

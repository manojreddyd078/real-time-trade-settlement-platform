param location string = resourceGroup().location
param environmentName string = 'trade-settlement'
param sqlAdminLogin string
@secure()
param sqlAdminPassword string

resource storage 'Microsoft.Storage/storageAccounts@2023-05-01' = {
  name: toLower(replace('${environmentName}storage', '-', ''))
  location: location
  sku: { name: 'Standard_LRS' }
  kind: 'StorageV2'
  properties: { allowBlobPublicAccess: false, minimumTlsVersion: 'TLS1_2' }
}

resource inputContainer 'Microsoft.Storage/storageAccounts/blobServices/containers@2023-05-01' = {
  name: '${storage.name}/default/input-trades'
  properties: { publicAccess: 'None' }
}

resource outputContainer 'Microsoft.Storage/storageAccounts/blobServices/containers@2023-05-01' = {
  name: '${storage.name}/default/processed-trades'
  properties: { publicAccess: 'None' }
}

resource sql 'Microsoft.Sql/servers@2022-05-01-preview' = {
  name: toLower('${environmentName}-sql')
  location: location
  properties: { administratorLogin: sqlAdminLogin, administratorLoginPassword: sqlAdminPassword, minimalTlsVersion: '1.2' }
}

resource database 'Microsoft.Sql/servers/databases@2022-05-01-preview' = {
  name: '${sql.name}/trade_settlement'
  location: location
  sku: { name: 'Basic', tier: 'Basic' }
  properties: { collation: 'SQL_Latin1_General_CP1_CI_AS' }
}

output storageAccountName string = storage.name
output sqlServerFqdn string = sql.properties.fullyQualifiedDomainName
output databaseName string = database.name

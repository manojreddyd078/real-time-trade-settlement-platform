param location string = resourceGroup().location
param environmentName string = 'trade-settlement'
param actionGroupId string

resource appInsights 'Microsoft.Insights/components@2020-02-02' = {
  name: '${environmentName}-insights'
  location: location
  kind: 'web'
  properties: { Application_Type: 'web', Request_Source: 'rest' }
}

resource apiErrorAlert 'Microsoft.Insights/metricAlerts@2018-03-01' = {
  name: '${environmentName}-api-errors'
  location: 'global'
  properties: {
    severity: 2
    enabled: true
    scopes: [appInsights.id]
    evaluationFrequency: 'PT5M'
    windowSize: 'PT15M'
    criteria: { 'odata.type': 'Microsoft.Azure.Monitor.SingleResourceMultipleMetricCriteria', allOf: [{ criterionType: 'StaticThresholdCriterion', metricName: 'requests/failed', operator: 'GreaterThan', threshold: 5, timeAggregation: 'Total' }] }
    actions: [{ actionGroupId: actionGroupId }]
  }
}

output instrumentationKey string = appInsights.properties.InstrumentationKey

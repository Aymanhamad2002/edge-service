custom_build(
    ref='edge-service',
    command='gradlew.bat bootBuildImage --imageName=%EXPECTED_REF%',
    deps=['build.gradle', 'src']
)
# Deploy
k8s_yaml(['k8s/deployment.yml', 'k8s/service.yml'])

# Manage
k8s_resource('edge-service', port_forwards=['9000'])
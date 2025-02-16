pluginManagement {
	repositories {
		maven { url = uri("https://repo.spring.io/milestone") }
		gradlePluginPortal()
	}
}
rootProject.name = "GeoChat"
include("auth-service")
//include("user-service") TODO: Will be added soon
//include("chat-service")
//include("notification-service")

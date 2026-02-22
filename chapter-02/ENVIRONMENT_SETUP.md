# 🛠️ Environment Setup Guide (Chapter 02)

This guide provides complete setup steps for the Bookstore backend development environment on:
- macOS
- Linux
- Windows

It covers:
- Java (JDK 25)
- Maven
- Git
- Docker Desktop (or Docker Engine on Linux)
- VS Code + required extensions
- Postman API Client

---

## Recommended Versions

| Tool | Recommended Version |
|------|----------------------|
| Java | 25 |
| Spring Boot | 4.0.3 |
| Maven | 3.9+ |
| Git | Latest stable |
| Docker | Latest stable |
| VS Code | Latest stable |
| Postman | Latest stable |

---

## macOS Setup

### 1) Install Homebrew (if not installed)

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

### 2) Install Java (JDK 25)

```bash
brew install openjdk@25
echo 'export PATH="/opt/homebrew/opt/openjdk@25/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
java -version
```

### 3) Install Maven

```bash
brew install maven
mvn -version
```

### 4) Install Git

```bash
brew install git
git --version
```

### 5) Install Docker Desktop

```bash
brew install --cask docker
```

Then open Docker Desktop once from Applications and verify:

```bash
docker --version
docker compose version
```

### 6) Install VS Code

```bash
brew install --cask visual-studio-code
```

Install extensions in VS Code (`Cmd+Shift+X`):
- Extension Pack for Java
- Spring Boot Extension Pack
- Docker
- REST Client (optional)

### 7) Install Postman

```bash
brew install --cask postman
```

---

## Linux Setup (Ubuntu/Debian)

### 1) Update package index

```bash
sudo apt update
```

### 2) Install Java (JDK 25)

```bash
sudo apt install -y openjdk-25-jdk
java -version
```

### 3) Install Maven

```bash
sudo apt install -y maven
mvn -version
```

### 4) Install Git

```bash
sudo apt install -y git
git --version
```

### 5) Install Docker Engine + Compose plugin

```bash
sudo apt install -y docker.io docker-compose-v2
sudo usermod -aG docker $USER
newgrp docker
docker --version
docker compose version
```

### 6) Install VS Code

```bash
sudo snap install code --classic
```

Install extensions in VS Code (`Ctrl+Shift+X`):
- Extension Pack for Java
- Spring Boot Extension Pack
- Docker
- REST Client (optional)

### 7) Install Postman

```bash
sudo snap install postman
```

---

## Windows Setup

### 1) Install Java (JDK 25)

Option A (Installer):
- Download and install JDK 25 from Oracle, Temurin, or Microsoft Build of OpenJDK.

Option B (Winget):

```powershell
winget install EclipseAdoptium.Temurin.25.JDK
java -version
```

### 2) Install Maven

```powershell
winget install Apache.Maven
mvn -version
```

If `mvn` is not found, reopen terminal and confirm Maven `bin` is in `PATH`.

### 3) Install Git

```powershell
winget install Git.Git
git --version
```

### 4) Install Docker Desktop

```powershell
winget install Docker.DockerDesktop
```

Start Docker Desktop and verify:

```powershell
docker --version
docker compose version
```

### 5) Install VS Code

```powershell
winget install Microsoft.VisualStudioCode
```

Install extensions in VS Code (`Ctrl+Shift+X`):
- Extension Pack for Java
- Spring Boot Extension Pack
- Docker
- REST Client (optional)

### 6) Install Postman

```powershell
winget install Postman.Postman
```

---

## Verify Your Full Environment

Run these commands and confirm all succeed:

```bash
java -version
mvn -version
git --version
docker --version
docker compose version
```

---

## Clone Book Repositories

```bash
git clone https://github.com/PacktPublishing/Spring-Boot-and-Angular-2E_Backend
git clone https://github.com/PacktPublishing/Spring-Boot-and-Angular-2E_Frontend
```

---

## Troubleshooting Tips

### macOS Troubleshooting

#### Java (JDK 25)
- If `java -version` shows an older JDK, run:
	```bash
	/usr/libexec/java_home -V
	export JAVA_HOME=$(/usr/libexec/java_home -v 25)
	export PATH="$JAVA_HOME/bin:$PATH"
	```
- If Homebrew Java is not detected, ensure this line exists in `~/.zshrc`:
	```bash
	export PATH="/opt/homebrew/opt/openjdk@25/bin:$PATH"
	```

#### Maven
- If Maven uses the wrong Java version, verify:
	```bash
	mvn -version
	echo $JAVA_HOME
	```
- If `mvn` is not found after install, reopen terminal or run `source ~/.zshrc`.

#### Git
- If Git asks repeatedly for credentials, use Git Credential Manager or SSH keys.
- If HTTPS clone fails due to certificate/network policy, check proxy settings and corporate certificates.

#### Docker Desktop
- If `docker` command exists but daemon is unavailable, start Docker Desktop from Applications.
- If port conflict occurs (e.g., 5432, 8080), identify process:
	```bash
	lsof -i :5432
	lsof -i :8080
	```
- If containers run slowly on Apple Silicon, prefer multi-arch images (or arm64 tags when available).

#### VS Code + Extensions
- If Java/Spring extensions do not activate, run `Cmd+Shift+P` → `Developer: Reload Window`.
- If Java language features fail, set JDK 25 in `Cmd+Shift+P` → `Java: Configure Java Runtime`.

#### Postman
- If local API calls fail, confirm the service is running and use exact base URL (including context path).
- If SSL errors appear for local dev endpoints, disable SSL verification only for local testing.

---

### Linux (Ubuntu/Debian) Troubleshooting

#### Java (JDK 25)
- If multiple JDKs are installed, select the correct one:
	```bash
	sudo update-alternatives --config java
	```
- Verify Java home path:
	```bash
	readlink -f $(which java)
	```

#### Maven
- If `mvn` is missing, ensure package install completed and binary is available in `/usr/bin/mvn`.
- If Maven Central downloads fail, check DNS/proxy/firewall configuration.

#### Git
- If `Permission denied (publickey)` occurs, configure SSH key and add it to GitHub.
- If line-ending issues appear, set:
	```bash
	git config --global core.autocrlf input
	```

#### Docker Engine + Compose
- If `permission denied` on Docker socket:
	```bash
	sudo usermod -aG docker $USER
	newgrp docker
	```
- If still failing, log out/in and verify:
	```bash
	groups
	docker ps
	```
- If service not running:
	```bash
	sudo systemctl status docker
	sudo systemctl start docker
	```

#### VS Code + Extensions
- If `snap` VS Code cannot access some folders/files, check snap confinement permissions.
- If Java extension pack is slow, install required JDK first, then reload VS Code.

#### Postman
- If Postman installed by snap cannot access local files/certs, adjust snap permissions.
- If API calls timeout to localhost, verify service bind address and firewall (`ufw`) rules.

---

### Windows Troubleshooting

#### Java (JDK 25)
- If `java` is not recognized, reopen PowerShell and verify `JAVA_HOME` and `Path` in System Environment Variables.
- Confirm active runtime:
	```powershell
	where java
	java -version
	```

#### Maven
- If `mvn` is not recognized, ensure Maven `bin` is in `Path`.
- Validate configuration:
	```powershell
	where mvn
	mvn -version
	```

#### Git
- If Git command unavailable in VS Code terminal, restart VS Code after installation.
- If corporate proxy blocks GitHub, configure proxy in Git:
	```powershell
	git config --global http.proxy http://proxy-host:proxy-port
	```

#### Docker Desktop
- If Docker fails to start, verify WSL2 is installed and enabled.
- If backend engine errors occur, switch Docker backend (WSL2/Hyper-V) from Docker settings.
- If `docker compose` is missing, update Docker Desktop to latest stable.

#### VS Code + Extensions
- If extensions fail to install, run VS Code as administrator once.
- If Java project import fails, select JDK 25 in `Java: Configure Java Runtime`.

#### Postman
- If requests to local APIs fail, check Windows Defender Firewall prompts for Java/Docker.
- Ensure URL uses the correct port and path (for example `/inventory` or `/user` context paths where applicable).

---

### Cross-Platform Quick Checks

- Verify tools in one go:
	```bash
	java -version && mvn -version && git --version && docker --version && docker compose version
	```
- If dependency downloads fail, check internet/proxy settings for Maven and Docker.
- If port already in use, stop conflicting service or run the app on a different port.

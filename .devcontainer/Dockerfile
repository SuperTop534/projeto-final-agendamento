# Imagem base do ambiente: Java 21 (Microsoft Build of OpenJDK) sobre Debian.
FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

# Correção: a imagem base traz o repositório de pacotes do Yarn com uma chave de
# assinatura vencida. Isso faz o "apt-get update" falhar e impede a instalação do
# Maven e do Docker. O Yarn não é usado neste projeto, então o repositório é removido.
USER root
RUN grep -rl "yarnpkg" /etc/apt/sources.list.d/ 2>/dev/null | xargs -r rm -f

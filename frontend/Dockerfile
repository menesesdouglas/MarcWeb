FROM node:20-alpine

WORKDIR /app

# Copia arquivos de dependência primeiro (cache de camada)
COPY package*.json ./

# Instala dependências (também executado no comando do compose, mas fica aqui para build isolado)
RUN npm install

# Copia o restante do código
COPY . .

EXPOSE 5173

# O comando de inicialização é sobrescrito no docker-compose.yml
CMD ["npm", "run", "dev", "--", "--host"]
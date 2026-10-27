# Build context: apps/web/
# Build: docker build -t vrls2-web ./apps/web

FROM node:20-alpine AS builder
WORKDIR /build
COPY package.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /build/dist/web/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

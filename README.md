# 🌱 HortLink Mobile

Aplicativo mobile desenvolvido para conectar pequenos produtores rurais diretamente aos consumidores finais, promovendo a agricultura familiar, o comércio local e o acesso facilitado a produtos agrícolas frescos.

O projeto busca eliminar intermediários na cadeia de comercialização, permitindo uma relação mais próxima entre produtores e consumidores, gerando benefícios para ambos os lados.

---

# 📱 Sobre o Projeto

O HortLink foi desenvolvido como projeto acadêmico do Centro Universitário Facens, com foco em tecnologia, inovação e impacto social.

A plataforma tem como objetivo aproximar:

* 👨‍🌾 Pequenos produtores rurais
* 🛒 Consumidores finais

através de um aplicativo intuitivo que permite a divulgação, busca e gerenciamento de produtos agrícolas.

Além disso, o projeto está alinhado com os Objetivos de Desenvolvimento Sustentável (ODS) da ONU:

* 🌾 ODS 2 — Fome Zero e Agricultura Sustentável
* 📈 ODS 8 — Trabalho Decente e Crescimento Econômico

---

# 🚀 Funcionalidades

## 🔐 Autenticação

* Cadastro de usuários
* Login de usuários
* Persistência de sessão
* Controle de acesso

## 🥬 Gerenciamento de Produtos

* Cadastro de produtos
* Edição de produtos
* Exclusão de produtos
* Listagem de produtos
* Visualização detalhada
* Upload de imagens

## 👤 Perfil de Usuário

* Cadastro de informações pessoais
* Gerenciamento de perfil
* Atualização de dados

## 🤖 Chatbot Inteligente

* Atendimento automatizado
* Respostas a dúvidas frequentes
* Auxílio na utilização da plataforma
* Suporte aos usuários

---

# 🏗️ Arquitetura do Projeto

Durante o desenvolvimento do HortLink foram exploradas duas arquiteturas distintas.

## Versão 1 — Firebase + Supabase

Primeira versão funcional desenvolvida para validação da ideia e prototipação rápida.

### Recursos

* Cadastro de usuários
* Login e autenticação
* Gerenciamento de produtos
* Upload de imagens
* Persistência de dados em nuvem

### Tecnologias

* Firebase Authentication
* Supabase PostgreSQL
* Supabase Storage
* REST API

---

## Versão 2 — Backend Próprio com Spring Boot

Com a evolução do projeto foi implementado o desenvolvimento de uma arquitetura baseada em backend próprio, proporcionando maior controle sobre regras de negócio, autenticação e escalabilidade.

### Recursos Implementados

* API REST
* Autenticação via JWT
* Controle de acesso com Spring Security
* Persistência de dados com JPA/Hibernate
* Estruturação das entidades de negócio

### Recursos Planejados

* Integração completa entre aplicativo e backend
* Gerenciamento avançado de produtores e consumidores
* Catálogo completo de produtos

> Esta arquitetura representa a evolução técnica do projeto e encontra-se em desenvolvimento funcional.

---

# 🛠️ Tecnologias Utilizadas

## 📱 Mobile

* Java
* Android Studio
* XML
* RecyclerView
* ConstraintLayout
* Material Design

## ☁️ Backend

* Spring Boot
* Spring Security
* JWT (JSON Web Token)
* JPA / Hibernate
* Maven

## 🗄️ Banco de Dados

* PostgreSQL
* Supabase PostgreSQL

## 🔥 Serviços

* Firebase Authentication
* Supabase Storage

## 🤖 Inteligência Artificial

* Botpress

## 📚 Bibliotecas

* OkHttp
* Glide
* Gson

---

# 📂 Estrutura Geral do Projeto

```text
app/
├── adapters/
├── data/
├── service/
├── ui/
├── util/
└── resources/
```

---

# 🎯 Objetivos do Projeto

* Incentivar a agricultura familiar
* Fortalecer o comércio local
* Facilitar o acesso a alimentos frescos
* Reduzir a dependência de intermediários
* Promover o desenvolvimento econômico regional
* Aplicar conhecimentos de desenvolvimento mobile e backend
* Explorar soluções de automação e inteligência artificial

---

# 🤖 Chatbot com Botpress

O HortLink conta com um chatbot desenvolvido utilizando Botpress para auxiliar usuários na utilização da plataforma.

### Possíveis aplicações

* Orientação para cadastro
* Informações sobre funcionalidades
* Esclarecimento de dúvidas frequentes
* Suporte automatizado

---

# 👨‍💻 Equipe

Projeto desenvolvido por:

* João
* Edson Sanchez
* Guilherme Aguiar Correia

Projeto acadêmico desenvolvido no Centro Universitário Facens.

---

# 📌 Status do Projeto

### Mobile (Versão Firebase + Supabase)

✅ Funcional

### Backend Spring Boot

MVP Funcional

---

# 🔗 Repositório Backend

### ☁️ Backend Spring Boot

```text
https://github.com/edsss2/HortLink
```

---

# 📄 Licença

Este projeto possui finalidade acadêmica e educacional.

Seu uso é destinado para fins de estudo, pesquisa e demonstração de tecnologias aplicadas ao desenvolvimento de software.

# Repository Guidelines

## Project Structure & Module Organization

本仓库采用多模块结构。后端由根 `pom.xml` 聚合，核心目录包括 `mars-common`、`mars-core`、`mars-infra`、`mars-api`、`mars-job` 与启动模块 `mars-starter`。管理后台位于 `mars-ui/src`，主要包含 `api`、`stores`、`router`、`views`。小程序端位于 `mars-uniapp/src`，常见目录有 `pages`、`components`、`api`、`hooks`、`store`。数据库脚本放在 `sql/`，运行截图与需求文档放在 `doc/`。

## Build, Test, and Development Commands

后端开发使用 Java 17 与 Maven：

```bash
mvn clean install
cd mars-starter
mvn spring-boot:run
```

管理后台使用 Vite：

```bash
cd mars-ui
npm install
npm run dev
npm run build
```

小程序端推荐使用 `pnpm`：

```bash
cd mars-uniapp
pnpm install
pnpm dev:mp
pnpm build:mp
pnpm test:run
```

## Coding Style & Naming Conventions

Java 代码保持 4 空格缩进，按 `controller -> service -> mapper -> entity` 分层，类名使用 `PascalCase`，方法与字段使用 `camelCase`。Vue / TypeScript 文件遵循现有命名：页面目录使用业务名，如 `src/views/system/user`；组件使用 `PascalCase.vue`；状态文件放在 `stores` 或 `store`。`mars-uniapp` 已配置 `ESLint`，提交前运行 `pnpm lint` 或 `pnpm lint:fix`。

## Testing Guidelines

当前自动化测试主要位于 `mars-uniapp`，使用 `Vitest`，测试文件命名为 `*.test.ts`，示例见 `src/utils/debounce.test.ts`。新增 hooks、store、纯函数工具时应补充同目录测试。后端目前未见完整 `src/test/java` 体系；若修改核心服务或接口，至少补充手工验证步骤与接口回归说明。

## Commit & Pull Request Guidelines

最近提交与 `mars-uniapp/.commitlintrc.cjs` 表明仓库采用 Conventional Commits，例如 `feat(auth): add mini-program login`、`fix(ui): correct banner upload`。PR 应说明变更范围、影响模块、验证命令，并在涉及 UI、小程序页面或 SQL 变更时附截图、录屏或脚本说明。避免提交真实密钥；配置变更请同步更新 `.env.example` 或 `mars-starter/src/main/resources/application-*.yml` 示例。

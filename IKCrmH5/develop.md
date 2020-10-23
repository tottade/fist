### commit 提交规范
* 用于说明 commit 的类别，使用下面7个标识。

  - feat：新功能（feature）

  - fix：修补bug

  - docs：文档（documentation）

  - style： 格式（不影响代码运行的变动）

  - refactor：重构（即不是新增功能，也不是修改bug的代码变动）

  - test：增加测试

  - chore：构建过程或辅助工具的变动

### 资源文件命名规则
* 用于说明资源文件的具体用途避免多次创建

  - drawable mipmap 类文件 :aa_bb_cc_dd
    -aa 用于描述文件用途，例如 ic(图标)，bg(背景)，selector(选择器)，rectangle(形状)，具体自定义控件名称等
    -bb 用于描述文件的具体名字
    -cc 用于描述文件的颜色等信息
    -dd 用于描述文件的圆角等信息

  - layout
    -activity_xxx :Activity布局
    -item_xxx :子项布局
    -layout_xxx :控件布局等其他布局

### 打包
  - productFlavors
    - 励销云
      - Build Variants -> lxyunDebug lxyunRelease
    - 爱客云
      - Build Variants -> ikyunDebug ikyunRelease
  - 配置文件 -> config.gradle
  - 资源文件
    - 励销云 -> src/main/res
    - 爱客云 -> src/main/res-ikh5 (同名资源覆盖励销云目录)


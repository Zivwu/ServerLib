# Step 11 — 创建 ClipboardController

> 目标：提供剪贴板同步和查询接口，返回统一 Result 格式。

---

## 文件位置

`src/main/java/com/ziv/echosync/features/clipboard/ClipboardController.java`

---

## 完整代码

```java
@RestController
@RequestMapping("/api/clipboard")
public class ClipboardController {

    @Autowired
    private ClipboardService clipboardService;

    @PostMapping("/sync")
    public Result<ClipboardVO> syncClipboard(@Valid @RequestBody ClipboardSyncDTO dto) {
        return Result.success(clipboardService.syncClipboard(dto));
    }

    @GetMapping("/latest")
    public Result<ClipboardVO> getLatest() {
        return Result.success(clipboardService.getLatest());
    }
}
```

---

## 测试

```bash
# 先登录获取 Token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"deviceName":"my-phone"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")

# 同步剪贴板
curl -X POST http://localhost:8080/api/clipboard/sync \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"content":"Hello from Android","deviceName":"my-phone"}'

# 获取最新
curl http://localhost:8080/api/clipboard/latest \
  -H "Authorization: Bearer $TOKEN"
```

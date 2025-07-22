@echo off
chcp 65001 > nul
cd /d %~dp0

echo.
echo ===============================
echo 📦 自動同步 Git 變更
echo ===============================
echo.

git branch
git status

echo.
REM 把所有檔案狀態加入暫存，包括 git-sync.bat
git add -A

echo.
git status

echo.
set /p commitmsg=📝 請輸入此次提交的訊息: 

REM 再次確認並加入 git-sync.bat（確保一定會加入）
git add git-sync.bat

git commit -m "%commitmsg%"
git push origin main

echo.
echo ✅ Git 同步完成！
pause

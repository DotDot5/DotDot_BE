// server.js

const express = require("express");
const nodemailer = require("nodemailer");
const puppeteer = require("puppeteer");
const cors = require("cors"); // 프론트엔드와의 통신을 위해 필요

const app = express();
const port = 3001; // Next.js 서버와 다른 포트 사용

// 미들웨어
app.use(express.json());
app.use(
  cors({
    origin: "http://localhost:3000", // Next.js 앱의 URL
  })
);

// 이메일 전송 API 엔드포인트
app.post("/send-email", async (req, res) => {
  const { to, subject, htmlContent } = req.body;

  if (!to || !subject || !htmlContent) {
    return res
      .status(400)
      .json({ success: false, message: "필수 데이터 누락" });
  }

  // 1. Puppeteer를 사용하여 HTML을 PDF로 변환
  let browser;
  try {
    browser = await puppeteer.launch({
      args: ["--no-sandbox", "--disable-setuid-sandbox"],
      headless: true, // 백그라운드에서 실행
    });
    const page = await browser.newPage();

    // HTML 콘텐츠를 페이지에 설정
    await page.setContent(htmlContent, { waitUntil: "networkidle0" });

    // PDF 생성
    const pdfBuffer = await page.pdf({
      format: "A4",
      printBackground: true,
    });

    await browser.close();

    // 2. Nodemailer를 사용하여 이메일 전송
    let transporter = nodemailer.createTransport({
      service: "gmail", // 예시: Gmail 사용
      host: "smtp.gmail.com",
      port: 587,
      secure: false,
      auth: {
        user: "당신의_이메일@gmail.com", // ⚠️ 실제 이메일 주소로 변경
        pass: "당신의_앱_비밀번호", // ⚠️ 실제 앱 비밀번호로 변경 (2단계 인증 활성화 시)
      },
      // 한글 깨짐 방지
      disableFileAccess: true,
      disableUrlAccess: true,
    });

    const mailOptions = {
      from: '"회의록 어시스턴트" <당신의_이메일@gmail.com>',
      to: to.join(", "), // 여러 수신자
      subject: subject,
      html: `
        <p>안녕하세요,</p>
        <p>요청하신 회의록을 첨부 파일로 보내드립니다. 아래의 내용은 이메일 본문입니다.</p>
        <p>감사합니다.</p>
        <br/>
      `,
      attachments: [
        {
          filename: "회의록.pdf",
          content: pdfBuffer,
          contentType: "application/pdf",
        },
      ],
    };

    await transporter.sendMail(mailOptions);

    res
      .status(200)
      .json({ success: true, message: "이메일이 성공적으로 발송되었습니다." });
  } catch (error) {
    console.error("이메일 발송 오류:", error);
    if (browser) await browser.close();
    res.status(500).json({ success: false, message: "이메일 발송 실패" });
  }
});

app.listen(port, () => {
  console.log(`백엔드 서버가 http://localhost:${port} 에서 실행 중입니다.`);
});

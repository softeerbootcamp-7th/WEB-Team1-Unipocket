import tailwindcss from '@tailwindcss/vite';
import { tanstackRouter } from '@tanstack/router-plugin/vite';
import react from '@vitejs/plugin-react-swc';
import { defineConfig, loadEnv } from 'vite';
import svgr from 'vite-plugin-svgr';

type User = {
  userId: string;
  email: string;
  nickname: string;
  profileImageUrl: string;
  socialProvider: string;
};

let isTokenExpired = true;
let reissueFailCount = 0; // 재발급 실패 카운트 (테스트용)

const mockUser = {
  userId: 'mock-uuid-1234',
  nickname: '임시 홍길동',
  email: 'mock-user@example.com',
  socialProvider: 'ADMIN',
  profileImageUrl: '',
} satisfies User;

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const apiBaseUrl = env.VITE_API_BASE_URL || 'https://api.unipocket.co.kr';

  return {
    base: '/',
    plugins: [
      tanstackRouter({
        target: 'react',
        autoCodeSplitting: true,
      }),
      react(),
      tailwindcss(),
      svgr({
        svgrOptions: {
          icon: true,
        },
        include: '**/*.svg',
      }),
    ],
    resolve: {
      alias: [{ find: '@', replacement: '/src' }],
    },
    server: {
      proxy: {
        '/api/users/me': {
          target: apiBaseUrl,
          changeOrigin: true,
          secure: false,
          bypass: (req, res) => {
            if (req.method === 'OPTIONS') return null; // 브라우저의 Preflight(OPTIONS) 요청은 통과시켜야 CORS 에러 안 남
            if (!res) return null;
            res.setHeader('Content-Type', 'application/json');

            // 토큰 만료 시 401 에러 응답
            if (isTokenExpired) {
              res.statusCode = 401;
              res.end(
                JSON.stringify({
                  message: '토큰이 만료되었습니다. (Mock)',
                  code: 'TOKEN_EXPIRED',
                }),
              );
              return req.url;
            }

            // 목 유저 응답
            res.statusCode = 200;
            res.end(JSON.stringify(mockUser));
            return req.url;
          },
        },

        '/api/auth/reissue': {
          target: apiBaseUrl,
          changeOrigin: true,
          secure: false,
          bypass: (req, res) => {
            if (req.method === 'OPTIONS') return null;
            if (!res) return null;

            res.setHeader('Content-Type', 'application/json');

            // reissueFailCount < 2 면
            // home에서 처음 401, 다시 재발급 시도 때 또 401, 로그인 페이지에서 200 받아서 자동으로 home으로 리다이렉트 처리

            // reissueFailCount < 3 면
            // home에서 처음 401, 다시 재발급 시도 때 또 401, 로그인 페이지에서 또 401 → 로그인 페이지에서 수동으로 로그인 시도해야 함
            if (reissueFailCount < 2) {
              reissueFailCount++;
              res.statusCode = 401;
              res.end(
                JSON.stringify({
                  message: '리프레시 토큰도 만료되었습니다. (Mock)',
                  code: 'REFRESH_TOKEN_EXPIRED',
                }),
              );
              return req.url;
            }

            // 그 다음 요청부터는 성공
            isTokenExpired = false;
            reissueFailCount = 0; // 카운트 리셋

            res.statusCode = 200;

            // 실제 백엔드처럼 쿠키 세팅
            res.setHeader('Set-Cookie', [
              'accessToken=mock-access-token; Path=/; HttpOnly; SameSite=Lax',
              'refreshToken=mock-refresh-token; Path=/; HttpOnly; SameSite=Lax',
            ]);

            res.end(JSON.stringify(mockUser));
            return req.url;
          },
        },

        '/api': {
          target: apiBaseUrl,
          changeOrigin: true,
          secure: false,
        },
      },
    },
  };
});

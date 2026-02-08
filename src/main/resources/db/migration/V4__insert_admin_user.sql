INSERT INTO users (
  email,
  name,
  password,
  role,
  enabled,
  banned
)
SELECT
  'admin@evolon.com',
  'Admin',
  '$2a$10$OeGmoEoYOR8bLPQDP2Jok.51BT00erTdkk2g.zrvh4DKNy5gT6dpe',
  'ADMIN',
  true,
  false
WHERE NOT EXISTS (
  SELECT 1 FROM users WHERE email = 'admin@evolon.com'
);

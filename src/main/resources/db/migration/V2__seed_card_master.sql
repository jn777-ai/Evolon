-- V2__seed_card_master.sql
-- card_master 初期データ（冪等）

INSERT INTO card_master (
    set_code,
    card_number,
    card_name,
    rarity,
    pack_name,
    printed_regulation
)
VALUES
    ('sv8a', '212/187', 'ニンフィアex', 'SAR', 'テラスタルフェスex', 'H'),
    ('sv8a', '206/187', 'グレイシアex', 'SAR', 'テラスタルフェスex', 'H'),
    ('sv8a', '200/187', 'リーフェアex', 'SAR', 'テラスタルフェスex', 'H'),
    ('sv8a', '202/187', 'ブースターex', 'SAR', 'テラスタルフェスex', 'H'),
    ('sv8a', '217/187', 'ブラッキーex', 'SAR', 'テラスタルフェスex', 'H'),
    ('sv8a', '205/187', 'シャワーズex', 'SAR', 'テラスタルフェスex', 'H'),
    ('sv8a', '209/187', 'サンダースex', 'SAR', 'テラスタルフェスex', 'H'),
    ('sv8a', '211/187', 'エーフィex', 'SAR', 'テラスタルフェスex', 'H'),
    ('sv8a', '069/187', 'ニンフィアex', 'RR', 'テラスタルフェスex', 'H'),
    ('sv6a', '072/064', 'ゾロア', 'SR', 'ナイトワンダラー', 'H'),
    ('sv8a', '020/184', 'オーガポンみどりのめんex', 'RR', 'テラスタルフェス', 'H'),
    ('sv8', '072/106', 'サザンドラex', 'RR', '超電ブレイカー', 'H')
ON CONFLICT (set_code, card_number) DO NOTHING;

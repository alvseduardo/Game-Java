import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Jogo extends JPanel {
    private static final int BASE_LARGURA = 1920;
    private static final int BASE_ALTURA = 1080;
    private static final boolean USAR_FULLSCREEN = true;
    private static final int FPS = 60;
    private static final double ESCALA_JOGADOR_VISUAL = 1.2;
    private static final double HITBOX_JOGADOR_ESCALA = 0.86;
    private static final boolean INVERTER_LADO_SPRITE = true;

    private static final double VELOCIDADE_ANDAR = 200.0;
    private static final double VELOCIDADE_CORRER = 320.0;
    private static final double VELOCIDADE_LENTA = 110.0;

    private static final int TILE_SIZE = 48;
    private static final double OBSTACULO_ESCALA = 0.7;

    private static final int TAMANHO_JOGADOR = 54;
    private static final int TAMANHO_CROISSANT = 26;
    private static final int TAMANHO_POWERUP = 26;

    private static final int INIMIGO_LENTO_TAM = 36;
    private static final int INIMIGO_RAPIDO_TAM = 24;
    private static final int INIMIGO_GUARDIAO_TAM = 32;
    private static final int INIMIGO_CACADOR_TAM = 28;

    private static final double INIMIGO_LENTO_VEL = 70.0;
    private static final double INIMIGO_RAPIDO_VEL = 150.0;
    private static final double INIMIGO_GUARDIAO_VEL = 110.0;
    private static final double INIMIGO_CACADOR_VEL = 135.0;

    private static final double RAIO_DETECCAO_BASE = 220.0;
    private static final double RAIO_DETECCAO_CORRER = 140.0;
    private static final double RAIO_GUARDIAO = 220.0;
    private static final long CACADOR_PERSEGUIR_MS = 2300;
    private static final long CACADOR_COOLDOWN_MS = 1900;
    private static final long PERSEGUIR_COMUM_MS = 1200;

    private static final double FOME_MAX_BASE = 100.0;
    private static final double FOME_DRENO_BASE = 2.2;
    private static final double FOME_DRENO_CORRER = 1.7;
    private static final double FOME_DRENO_ZONA = 2.6;
    private static final double FOME_GANHO_CROISSANT = 18.0;
    private static final double FOME_REGEN_POWERUP = 4.6;
    private static final double FOME_CRITICA_RATIO = 0.2;
    private static final long FOME_DANO_INTERVALO_MS = 1200;

    private static final double STAMINA_MAX_BASE = 100.0;
    private static final double STAMINA_CUSTO_CORRER = 18.0;
    private static final double STAMINA_REGEN = 16.0;
    private static final double STAMINA_RETORNO_LIMIAR = 0.3;

    private static final long TEMPO_LENTO_MS = 900;
    private static final long DANO_INVENCIVEL_MS = 3000;
    private static final long DANO_PISCA_MS = 900;
    private static final long CONFUSAO_MS = 1400;
    private static final long INIMIGO_LENTO_DANO_MS = 1600;
    private static final double INIMIGO_LENTO_DANO_MULT = 0.55;

    private static final int PISTOLA_RAIO = 260;
    private static final long PISTOLA_COOLDOWN_MS = 200;

    private static final int VIDAS_INICIAIS = 3;
    private static final int VIDAS_MAX = 5;
    private static final int PONTOS_PARA_NOVO_INIMIGO = 4;
    private static final int PONTOS_PARA_NOVO_NIVEL = 6;

    private static final long POWERUP_DURACAO_MS = 6500;
    private static final long POWERUP_SPAWN_BASE_MS = 8000;
    private static final long POWERUP_SPAWN_MIN_MS = 3000;
    private static final int CUSTO_SKIN_LEGENDARIA = 45;
    private static final int VIDAS_LEGENDARIA = 10;
    private static final double POWERUP_DURACAO_MULTIPLICADOR_SKIN = 1.8;
    private static final long ITEM_SPAWN_BASE_MS = 8000;
    private static final long ITEM_SPAWN_MIN_MS = 3000;
    private static final long EXTRA_SPAWN_INICIAL_MS = 11000;
    private static final long EXTRA_SPAWN_MIN_MS = 900;
    private static final double EXTRA_SPAWN_CURVE_SEG = 90.0;

    private static final int MAX_TENTATIVAS_SPAWN = 260;
    private static final long TRANSICAO_MS = 450;

    private static final int PARTICULAS_CROISSANT = 18;
    private static final int PARTICULAS_DANO = 14;

    private static final int NUM_OBSTACULOS_RANDOM = 8;
    private static final int NUM_CORREDORES_RANDOM = 4;
    private static final int NUM_BURACOS = 4;
    private static final int NUM_ESCORREGADIOS = 3;
    private static final int NUM_ARMADILHAS = 3;
    private static final int NUM_ZONAS_FOME = 2;

    private static final double UPGRADE_FOME_INC = 12.0;
    private static final double UPGRADE_STAMINA_INC = 12.0;
    private static final double UPGRADE_VELOCIDADE_MULT = 0.06;
    private static final double UPGRADE_CROISSANT_MULT = 0.15;

    private static final int LOJA_CUSTO_FOME = 6;
    private static final int LOJA_CUSTO_STAMINA = 6;
    private static final int LOJA_CUSTO_VELOCIDADE = 8;
    private static final int LOJA_CUSTO_CROISSANT = 7;

    private enum EstadoJogo { MENU, JOGANDO, PAUSADO, GAME_OVER, LOJA }
    private enum Direcao { CIMA, BAIXO, ESQUERDA, DIREITA }
    private enum PowerUpTipo { VELOCIDADE, INVENCIVEL, REGEN_FOME, PISTOLA, VIDA }
    private enum InimigoTipo { LENTO, RAPIDO, GUARDIAO, CACADOR }
    private enum MissaoTipo { CROISSANTS_SEM_DANO, CROISSANTS_TEMPO, CROISSANTS_CORRER, PEGAR_POWERUPS }
    private enum PerigoTipo { BURACO, ESCORREGADIO, ARMADILHA, ZONA_FOME }
    private enum Animacao { PARADO, ANDAR, CORRER, DANO }
    private enum UpgradeTipo { FOME, STAMINA, VELOCIDADE, CROISSANT }

    private int largura = BASE_LARGURA;
    private int altura = BASE_ALTURA;

    private Rectangle2D corredorHorizontal;
    private Rectangle2D corredorVertical;
    private Rectangle2D areaSpawn;

    private EstadoJogo estado = EstadoJogo.MENU;
    private EstadoJogo estadoAlvo = EstadoJogo.MENU;
    private float fadeAlpha = 0f;
    private int fadeDirecao = 0;

    private final Jogador seuBarriga;
    private final List<Obstaculo> obstaculos = new ArrayList<>();
    private final List<Perigo> perigos = new ArrayList<>();
    private final List<Inimigo> inimigos = new ArrayList<>();
    private final List<Efeito> efeitos = new ArrayList<>();
    private final List<Particula> particulas = new ArrayList<>();

    private Croissant croissant;
    private PowerUp powerUpAtual;
    private PowerUp itemEspecialAtual;

    private int pontuacao;
    private int melhorPontuacao;
    private int vidas;
    private int croissantsColetados;
    private int pontosLoja;
    private int pontosTotais;
    private int bonusLojaRecebido;
    private int nivelDificuldade;
    private int contadorSegredoVolume;
    private long segredoVolumeMensagemAteMs;
    private int proximoNivelEm;
    private int proximoInimigoScore;

    private int upgradeFomeNivel;
    private int upgradeStaminaNivel;
    private int upgradeVelocidadeNivel;
    private int upgradeCroissantNivel;

    private double fomeMax = FOME_MAX_BASE;
    private double fomeAtual = fomeMax;
    private double staminaMax = STAMINA_MAX_BASE;
    private double staminaAtual = staminaMax;
    private boolean staminaExausta;

    private boolean emEscorregadio;
    private boolean emZonaFome;
    private boolean skinQuicoComprada;

    private long proximoDanoFomeMs;

    private long lentoAteMs;
    private long invencivelAteMs;
    private long danoAteMs;
    private long confusaoAteMs;

    private long powerUpAteMs;
    private PowerUpTipo powerUpAtivo;
    private int balasPistola;
    private long proximoTiroMs;

    private long proximoSpawnPowerUpMs;
    private long proximoSpawnItemMs;
    private long jogoInicioMs;
    private long proximoSpawnInimigoExtraMs;

    private Missao missaoAtual;
    private long proximaMissaoMs;

    private boolean cima;
    private boolean baixo;
    private boolean esquerda;
    private boolean direita;
    private boolean correr;

    private double animCroissantTempo;
    private double animPowerUpTempo;

    private final Random rng = new Random();

    private BufferedImage imagemBarriga;
    private BufferedImage imagemCroissant;
    private BufferedImage imagemCoracao;
    private BufferedImage imagemFundo;
    private BufferedImage imagemChaoTile;
    private BufferedImage imagemParedeTile;
    private BufferedImage imagemPowerVelocidade;
    private BufferedImage imagemPowerInvencivel;
    private BufferedImage imagemPowerFome;
    private BufferedImage imagemPowerPistola;
    private BufferedImage imagemPowerVida;
    private BufferedImage imagemBuraco;
    private BufferedImage imagemGelo;
    private BufferedImage imagemFogo;
    private BufferedImage imagemArmadilha;
    private BufferedImage imagemSkinQuico;
    private BufferedImage[][] framesParado;
    private BufferedImage[][] framesAndar;
    private BufferedImage[][] framesCorrer;
    private BufferedImage[][] framesDano;
    private BufferedImage[][] framesParadoDefault;
    private BufferedImage[][] framesAndarDefault;
    private BufferedImage[][] framesCorrerDefault;
    private BufferedImage[][] framesDanoDefault;
    private BufferedImage[][] framesParadoSkin;
    private BufferedImage[][] framesAndarSkin;
    private BufferedImage[][] framesCorrerSkin;
    private BufferedImage[][] framesDanoSkin;
    private final EnumMap<InimigoTipo, BufferedImage> imagemInimigos = new EnumMap<>(InimigoTipo.class);
    private TexturePaint texturaChao;
    private TexturePaint texturaParede;
    private BufferedImage tileWallUnico;
    private BufferedImage tileWallCenter;
    private BufferedImage tileWallEdgeTop;
    private BufferedImage tileWallEdgeBottom;
    private BufferedImage tileWallEdgeLeft;
    private BufferedImage tileWallEdgeRight;
    private BufferedImage tileWallCornerTL;
    private BufferedImage tileWallCornerTR;
    private BufferedImage tileWallCornerBL;
    private BufferedImage tileWallCornerBR;
    private BufferedImage[] tilesChaoVariacoes;
    private boolean[][] tilesParede;
    private int[][] tilesChaoVar;
    private int tilesCols;
    private int tilesRows;
    private Font fonteTitulo;
    private Font fonteSubtitulo;
    private Font fonteUI;
    private Font fonteHUD;
    private Font fonteHUDMini;

    private Rectangle botaoIniciar = new Rectangle(0, 0, 0, 0);
    private Rectangle botaoLoja = new Rectangle(0, 0, 0, 0);
    private Rectangle botaoLojaVoltar = new Rectangle(0, 0, 0, 0);
    private Rectangle botaoLojaJogar = new Rectangle(0, 0, 0, 0);
    private Rectangle botaoSairMenu = new Rectangle(0, 0, 0, 0);
    private Rectangle botaoSkinLegendaria = new Rectangle(0, 0, 0, 0);
    private Rectangle botaoVolumeMais = new Rectangle(0, 0, 0, 0);
    private Rectangle botaoVolumeMenos = new Rectangle(0, 0, 0, 0);
    private Rectangle botaoSairGameOver = new Rectangle(0, 0, 0, 0);
    private Rectangle botaoSairLoja = new Rectangle(0, 0, 0, 0);
    private Rectangle botaoPausaContinuar = new Rectangle(0, 0, 0, 0);
    private Rectangle botaoPausaReiniciar = new Rectangle(0, 0, 0, 0);
    private Rectangle botaoPausaSair = new Rectangle(0, 0, 0, 0);
    private Rectangle botaoOpcoesMenu = new Rectangle(0, 0, 0, 0);
    private Rectangle botaoOpcoesGameOver = new Rectangle(0, 0, 0, 0);
    private Rectangle botaoOpcoesPausa = new Rectangle(0, 0, 0, 0);
    private Rectangle botaoFecharOpcoes = new Rectangle(0, 0, 0, 0);
    private final Rectangle[] botoesCompra = new Rectangle[4];
    private final LojaItem[] lojaItens = new LojaItem[4];
    private Rectangle painelOpcoesBounds = new Rectangle(0, 0, 0, 0);
    private boolean opcoesVisiveis;

    private long ultimoTempoNs;
    private final Timer timer;
    private final AudioManager audio = new AudioManager();

    public Jogo() {
        setPreferredSize(new Dimension(BASE_LARGURA, BASE_ALTURA));
        setBackground(new Color(20, 20, 24));
        setFocusable(true);
        setDoubleBuffered(true);

        seuBarriga = new Jogador("Seu Barriga", 80, 80, TAMANHO_JOGADOR);
        carregarImagens();
        configurarFontes();
        configurarLojaItens();
        criarTexturas();
        configurarTeclado();
        configurarMouse();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                atualizarDimensoes();
            }
        });

        resetarJogo();

        ultimoTempoNs = System.nanoTime();
        timer = new Timer(1000 / FPS, e -> gameLoop());
        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        atualizarDimensoes();
    }

    private void atualizarDimensoes() {
        int w = getWidth() > 0 ? getWidth() : BASE_LARGURA;
        int h = getHeight() > 0 ? getHeight() : BASE_ALTURA;
        if (w != largura || h != altura) {
            largura = w;
            altura = h;
            gerarMapa();
            ajustarJogadorDentroMapa();
        }
    }

    private void ajustarJogadorDentroMapa() {
        seuBarriga.x = clamp(seuBarriga.x, 0, Math.max(0, largura - seuBarriga.tamanho));
        seuBarriga.y = clamp(seuBarriga.y, 0, Math.max(0, altura - seuBarriga.tamanho));
    }

    private void gerarMapa() {
        obstaculos.clear();
        perigos.clear();

        double w = largura;
        double h = altura;

        double corredorW = Math.max(140, w * 0.12);
        double corredorH = Math.max(140, h * 0.12);
        corredorVertical = new Rectangle2D.Double(w * 0.5 - corredorW / 2.0, 0, corredorW, h);
        corredorHorizontal = new Rectangle2D.Double(0, h * 0.5 - corredorH / 2.0, w, corredorH);
        double spawnSize = Math.max(Math.min(w, h) * 0.25, Math.max(220, Math.min(w, h) * 0.2));
        double spawnX = Math.max(40, w / 2.0 - spawnSize / 2.0);
        double spawnY = Math.max(40, h / 2.0 - spawnSize / 2.0);
        areaSpawn = new Rectangle2D.Double(spawnX, spawnY, Math.min(spawnSize, w - spawnX * 2), Math.min(spawnSize, h - spawnY * 2));

        adicionarObstaculoRel(0.12, 0.12, 0.2, 0.04);
        adicionarObstaculoRel(0.35, 0.2, 0.03, 0.28);
        adicionarObstaculoRel(0.15, 0.62, 0.22, 0.05);
        adicionarObstaculoRel(0.62, 0.12, 0.22, 0.05);
        adicionarObstaculoRel(0.72, 0.52, 0.04, 0.25);

        criarObstaculosProcedurais();
        criarPerigosProcedurais();
        atualizarTilemap();
    }

    private void adicionarObstaculoRel(double rx, double ry, double rw, double rh) {
        double baseW = rw * largura;
        double baseH = rh * altura;
        double w = baseW * OBSTACULO_ESCALA;
        double h = baseH * OBSTACULO_ESCALA;
        double x = rx * largura + (baseW - w) / 2.0;
        double y = ry * altura + (baseH - h) / 2.0;
        obstaculos.add(new Obstaculo(x, y, w, h));
    }

    private void criarObstaculosProcedurais() {
        double margem = 30;
        for (int i = 0; i < NUM_CORREDORES_RANDOM; i++) {
            for (int t = 0; t < 40; t++) {
                boolean vertical = rng.nextBoolean();
                double w = vertical ? 40 + rng.nextInt(50) : 180 + rng.nextInt(180);
                double h = vertical ? 180 + rng.nextInt(220) : 40 + rng.nextInt(50);
                w *= OBSTACULO_ESCALA;
                h *= OBSTACULO_ESCALA;
                Rectangle2D rect = new Rectangle2D.Double(
                        margem + rng.nextDouble() * (largura - w - 2 * margem),
                        margem + rng.nextDouble() * (altura - h - 2 * margem),
                        w, h);
                if (tentarAdicionarObstaculo(rect)) {
                    break;
                }
            }
        }

        for (int i = 0; i < NUM_OBSTACULOS_RANDOM; i++) {
            for (int t = 0; t < 40; t++) {
                double w = 80 + rng.nextInt(140);
                double h = 60 + rng.nextInt(140);
                w *= OBSTACULO_ESCALA;
                h *= OBSTACULO_ESCALA;
                Rectangle2D rect = new Rectangle2D.Double(
                        margem + rng.nextDouble() * (largura - w - 2 * margem),
                        margem + rng.nextDouble() * (altura - h - 2 * margem),
                        w, h);
                if (tentarAdicionarObstaculo(rect)) {
                    break;
                }
            }
        }
    }

    private boolean tentarAdicionarObstaculo(Rectangle2D rect) {
        if (intersectaReservas(rect)) {
            return false;
        }
        for (Obstaculo o : obstaculos) {
            if (o.getBounds().intersects(rect)) {
                return false;
            }
        }
        obstaculos.add(new Obstaculo(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
        return true;
    }

    private void criarPerigosProcedurais() {
        adicionarPerigos(PerigoTipo.BURACO, NUM_BURACOS, 60, 110, false);
        adicionarPerigos(PerigoTipo.ESCORREGADIO, NUM_ESCORREGADIOS, 120, 200, true);
        adicionarPerigos(PerigoTipo.ZONA_FOME, NUM_ZONAS_FOME, 140, 220, true);
        adicionarPerigos(PerigoTipo.ARMADILHA, NUM_ARMADILHAS, 80, 120, false);
    }

    private void atualizarTilemap() {
        tilesCols = (int) Math.ceil(largura / (double) TILE_SIZE);
        tilesRows = (int) Math.ceil(altura / (double) TILE_SIZE);
        tilesParede = new boolean[tilesCols][tilesRows];
        tilesChaoVar = new int[tilesCols][tilesRows];
        if (tilesChaoVariacoes == null || tilesChaoVariacoes.length == 0) {
            prepararTilesChao();
        }
        int variacoes = tilesChaoVariacoes != null ? tilesChaoVariacoes.length : 1;
        for (int c = 0; c < tilesCols; c++) {
            for (int r = 0; r < tilesRows; r++) {
                double x = c * TILE_SIZE;
                double y = r * TILE_SIZE;
                Rectangle2D tileRect = new Rectangle2D.Double(x, y, TILE_SIZE, TILE_SIZE);
                boolean parede = false;
                for (Obstaculo o : obstaculos) {
                    if (o.getBounds().intersects(tileRect)) {
                        parede = true;
                        break;
                    }
                }
                tilesParede[c][r] = parede;
                tilesChaoVar[c][r] = variacoes > 0 ? rng.nextInt(variacoes) : 0;
            }
        }
    }

    private boolean isParedeTile(int c, int r) {
        if (tilesParede == null) {
            return false;
        }
        if (c < 0 || r < 0 || c >= tilesCols || r >= tilesRows) {
            return false;
        }
        return tilesParede[c][r];
    }

    private void adicionarPerigos(PerigoTipo tipo, int quantidade, double minTam, double maxTam, boolean retangular) {
        double margem = 40;
        long agoraMs = System.currentTimeMillis();
        verificarSpawnInimigoExtra(agoraMs);
        for (int i = 0; i < quantidade; i++) {
            for (int t = 0; t < 60; t++) {
                double w = minTam + rng.nextDouble() * (maxTam - minTam);
                double h = retangular ? (minTam + rng.nextDouble() * (maxTam - minTam)) : w;
                Rectangle2D rect = new Rectangle2D.Double(
                        margem + rng.nextDouble() * (largura - w - 2 * margem),
                        margem + rng.nextDouble() * (altura - h - 2 * margem),
                        w, h);
                Perigo p = new Perigo(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), tipo);
                if (tentarAdicionarPerigo(p)) {
                    if (tipo == PerigoTipo.ARMADILHA) {
                        long ativo = 900 + rng.nextInt(700);
                        long inativo = 900 + rng.nextInt(900);
                        p.configurarCiclo(agoraMs + rng.nextInt(500), ativo, inativo);
                    }
                    break;
                }
            }
        }
    }

    private boolean tentarAdicionarPerigo(Perigo perigo) {
        Rectangle2D rect = perigo.getBounds();
        if (intersectaReservas(rect)) {
            return false;
        }
        for (Obstaculo o : obstaculos) {
            if (o.getBounds().intersects(rect)) {
                return false;
            }
        }
        for (Perigo p : perigos) {
            if (p.getBounds().intersects(rect)) {
                return false;
            }
        }
        perigos.add(perigo);
        return true;
    }

    private boolean intersectaReservas(Rectangle2D rect) {
        if (areaSpawn != null && rect.intersects(areaSpawn)) {
            return true;
        }
        if (corredorHorizontal != null && rect.intersects(corredorHorizontal)) {
            return true;
        }
        if (corredorVertical != null && rect.intersects(corredorVertical)) {
            return true;
        }
        return false;
    }

    private void configurarLojaItens() {
        lojaItens[0] = new LojaItem(UpgradeTipo.FOME, "Fome maxima", LOJA_CUSTO_FOME);
        lojaItens[1] = new LojaItem(UpgradeTipo.STAMINA, "Stamina maxima", LOJA_CUSTO_STAMINA);
        lojaItens[2] = new LojaItem(UpgradeTipo.VELOCIDADE, "Velocidade base", LOJA_CUSTO_VELOCIDADE);
        lojaItens[3] = new LojaItem(UpgradeTipo.CROISSANT, "Eficiencia do croissant", LOJA_CUSTO_CROISSANT);
        for (int i = 0; i < botoesCompra.length; i++) {
            botoesCompra[i] = new Rectangle(0, 0, 0, 0);
        }
    }

    private void comprarUpgradePorIndice(int indice) {
        if (indice < 0 || indice >= lojaItens.length) {
            return;
        }
        comprarUpgrade(lojaItens[indice].tipo);
    }

    private boolean comprarUpgrade(UpgradeTipo tipo) {
        int custo = getCustoUpgrade(tipo);
        if (pontosLoja < custo) {
            return false;
        }
        pontosLoja -= custo;
        aplicarUpgrade(tipo);
        return true;
    }

    private void comprarSkinLegendaria() {
        if (skinQuicoComprada) {
            return;
        }
        if (pontosLoja < CUSTO_SKIN_LEGENDARIA) {
            return;
        }
        pontosLoja -= CUSTO_SKIN_LEGENDARIA;
        skinQuicoComprada = true;
        atualizarFramesJogador();
    }

    private int getNivelUpgrade(UpgradeTipo tipo) {
        switch (tipo) {
            case FOME:
                return upgradeFomeNivel;
            case STAMINA:
                return upgradeStaminaNivel;
            case VELOCIDADE:
                return upgradeVelocidadeNivel;
            case CROISSANT:
                return upgradeCroissantNivel;
            default:
                return 0;
        }
    }

    private void aplicarUpgrade(UpgradeTipo tipo) {
        switch (tipo) {
            case FOME:
                upgradeFomeNivel += 1;
                break;
            case STAMINA:
                upgradeStaminaNivel += 1;
                break;
            case VELOCIDADE:
                upgradeVelocidadeNivel += 1;
                break;
            case CROISSANT:
                upgradeCroissantNivel += 1;
                break;
            default:
                break;
        }
    }

    private int getCustoUpgrade(UpgradeTipo tipo) {
        int nivel = getNivelUpgrade(tipo);
        int base = 0;
        for (LojaItem item : lojaItens) {
            if (item != null && item.tipo == tipo) {
                base = item.custoBase;
                break;
            }
        }
        if (base <= 0) {
            base = LOJA_CUSTO_FOME;
        }
        return (int) Math.round(base * Math.pow(1.35, nivel));
    }

    private String getDescricaoUpgrade(UpgradeTipo tipo) {
        switch (tipo) {
            case FOME:
                return "+ " + (int) UPGRADE_FOME_INC + " fome maxima";
            case STAMINA:
                return "+ " + (int) UPGRADE_STAMINA_INC + " stamina maxima";
            case VELOCIDADE:
                return "+ " + (int) Math.round(UPGRADE_VELOCIDADE_MULT * 100) + "% velocidade base";
            case CROISSANT:
                return "+ " + (int) Math.round(UPGRADE_CROISSANT_MULT * 100) + "% fome por croissant";
            default:
                return "";
        }
    }

    private void aplicarUpgrades() {
        fomeMax = FOME_MAX_BASE + upgradeFomeNivel * UPGRADE_FOME_INC;
        staminaMax = STAMINA_MAX_BASE + upgradeStaminaNivel * UPGRADE_STAMINA_INC;
    }

    private double getMultiplicadorVelocidade() {
        return 1.0 + upgradeVelocidadeNivel * UPGRADE_VELOCIDADE_MULT;
    }

    private double getGanhoCroissant() {
        return FOME_GANHO_CROISSANT * (1.0 + upgradeCroissantNivel * UPGRADE_CROISSANT_MULT);
    }

    private boolean temSkinLegendaria() {
        return skinQuicoComprada;
    }

    private int getVidasIniciaisComSkin() {
        return temSkinLegendaria() ? VIDAS_LEGENDARIA : VIDAS_INICIAIS;
    }

    private int getMaxVidasComSkin() {
        return temSkinLegendaria() ? VIDAS_LEGENDARIA : VIDAS_MAX;
    }

    private long getDuracaoPowerUpAtual() {
        double mult = temSkinLegendaria() ? POWERUP_DURACAO_MULTIPLICADOR_SKIN : 1.0;
        return (long) (POWERUP_DURACAO_MS * mult);
    }

    private int getBonusForca() {
        return temSkinLegendaria() ? 3 : 1;
    }

    private int getBalasPorPowerUpPistola() {
        return temSkinLegendaria() ? 5 : 1;
    }

    private Color getCorPowerUp(PowerUpTipo tipo) {
        if (tipo == PowerUpTipo.VELOCIDADE) {
            return new Color(120, 200, 255);
        }
        if (tipo == PowerUpTipo.INVENCIVEL) {
            return new Color(255, 210, 120);
        }
        if (tipo == PowerUpTipo.PISTOLA) {
            return new Color(200, 200, 220);
        }
        if (tipo == PowerUpTipo.VIDA) {
            return new Color(150, 240, 150);
        }
        return new Color(120, 220, 140);
    }

    private String getNomePowerUp(PowerUpTipo tipo) {
        if (tipo == null) {
            return "Power-up";
        }
        switch (tipo) {
            case VELOCIDADE:
                return "Velocidade";
            case INVENCIVEL:
                return "Invencivel";
            case REGEN_FOME:
                return "Regeneracao";
            case PISTOLA:
                return "Pistola";
            case VIDA:
                return "Vida";
            default:
                return "Power-up";
        }
    }
    
    private void carregarImagens() {
        imagemBarriga = carregarImagem(
                "assets/hd/seu_barriga_hd.png", "assets/hd/seu_barriga_hd.jpg", "assets/hd/seu_barriga_hd.jpeg",
                "assets/hd/seu_barriga.png", "assets/hd/seu_barriga.jpg", "assets/hd/seu_barriga.jpeg",
                "assets/seu_barriga_hd.png", "assets/seu_barriga_hd.jpg", "assets/seu_barriga_hd.jpeg",
                "seu_barriga_hd.png", "seu_barriga_hd.jpg", "seu_barriga_hd.jpeg",
                "assets/seu_barriga.png", "assets/seu_barriga.jpg", "assets/seu_barriga.jpeg",
                "seu_barriga.png", "seu_barriga.jpg", "seu_barriga.jpeg");
        imagemCroissant = carregarImagem(
                "assets/hd/croissant_hd.png", "assets/hd/croissant_hd.jpg", "assets/hd/croissant_hd.jpeg",
                "assets/hd/croissant.png", "assets/hd/croissant.jpg", "assets/hd/croissant.jpeg",
                "assets/croissant_hd.png", "assets/croissant_hd.jpg", "assets/croissant_hd.jpeg",
                "croissant_hd.png", "croissant_hd.jpg", "croissant_hd.jpeg",
                "assets/croissant.png", "assets/croissant.jpg", "assets/croissant.jpeg",
                "croissant.png", "croissant.jpg", "croissant.jpeg",
                "assets/croassaint.png", "assets/croassaint.jpg", "assets/croassaint.jpeg",
                "croassaint.png", "croassaint.jpg", "croassaint.jpeg");
        imagemFundo = carregarImagem(
                "assets/hd/background_hd.png", "assets/hd/background_hd.jpg", "assets/hd/background_hd.jpeg",
                "assets/hd/background.png", "assets/hd/background.jpg", "assets/hd/background.jpeg",
                "assets/background_hd.png", "assets/background_hd.jpg", "assets/background_hd.jpeg",
                "background_hd.png", "background_hd.jpg", "background_hd.jpeg",
                "assets/background.png", "assets/background.jpg", "assets/background.jpeg",
                "background.png", "background.jpg", "background.jpeg");
        imagemChaoTile = carregarImagem(
                "assets/hd/floor_tile_hd.png", "assets/hd/floor_tile_hd.jpg", "assets/hd/floor_tile_hd.jpeg",
                "assets/floor_tile_hd.png", "assets/floor_tile_hd.jpg", "assets/floor_tile_hd.jpeg",
                "floor_tile_hd.png", "floor_tile_hd.jpg", "floor_tile_hd.jpeg",
                "assets/floor_tile.png", "assets/floor_tile.jpg", "assets/floor_tile.jpeg",
                "floor_tile.png", "floor_tile.jpg", "floor_tile.jpeg");
        imagemParedeTile = carregarImagem(
                "assets/hd/wall_tile_hd.png", "assets/hd/wall_tile_hd.jpg", "assets/hd/wall_tile_hd.jpeg",
                "assets/wall_tile_hd.png", "assets/wall_tile_hd.jpg", "assets/wall_tile_hd.jpeg",
                "wall_tile_hd.png", "wall_tile_hd.jpg", "wall_tile_hd.jpeg",
                "assets/wall_tile.png", "assets/wall_tile.jpg", "assets/wall_tile.jpeg",
                "wall_tile.png", "wall_tile.jpg", "wall_tile.jpeg");
        imagemPowerVelocidade = carregarImagem(
                "assets/hd/power_velocidade_hd.png", "assets/power_velocidade_hd.png", "power_velocidade_hd.png",
                "power_velocidade.png", "assets/power_velocidade.png");
        imagemPowerInvencivel = carregarImagem(
                "assets/hd/power_invencivel_hd.png", "assets/power_invencivel_hd.png", "power_invencivel_hd.png",
                "power_invencivel.png", "assets/power_invencivel.png");
        imagemPowerFome = carregarImagem(
                "assets/hd/power_fome_hd.png", "assets/power_fome_hd.png", "power_fome_hd.png",
                "power_fome.png", "assets/power_fome.png");
        imagemPowerPistola = carregarImagem(
                "assets/hd/power_pistola_hd.png", "assets/power_pistola_hd.png", "power_pistola_hd.png",
                "power_pistola.png", "assets/power_pistola.png");
        imagemPowerVida = carregarImagem(
                "assets/hd/power_vida_hd.png", "assets/power_vida_hd.png", "power_vida_hd.png",
                "power_vida.png", "assets/power_vida.png", "vida.png", "assets/vida.png");
        imagemCoracao = carregarImagem(
                "assets/hd/heart_hd.png", "assets/heart_hd.png", "heart_hd.png",
                "heart.png", "assets/heart.png");
        imagemBuraco = carregarImagem(
                "assets/hd/buraco_hd.png", "assets/buraco_hd.png", "buraco_hd.png",
                "assets/hd/buraco.png", "assets/buraco.png", "buraco.png",
                "assets/hd/hole_hd.png", "assets/hole_hd.png", "hole_hd.png",
                "assets/hd/hole.png", "assets/hole.png", "hole.png");
        imagemGelo = carregarImagem(
                "assets/hd/gelo_hd.png", "assets/gelo_hd.png", "gelo_hd.png",
                "assets/hd/gelo.png", "assets/gelo.png", "gelo.png",
                "assets/hd/ice_hd.png", "assets/ice_hd.png", "ice_hd.png",
                "assets/hd/ice.png", "assets/ice.png", "ice.png");
        imagemFogo = carregarImagem(
                "assets/hd/fogo_hd.png", "assets/fogo_hd.png", "fogo_hd.png",
                "assets/hd/fogo.png", "assets/fogo.png", "fogo.png",
                "assets/hd/fire_hd.png", "assets/fire_hd.png", "fire_hd.png",
                "assets/hd/fire.png", "assets/fire.png", "fire.png");
        imagemArmadilha = carregarImagem(
                "assets/hd/armadilha_hd.png", "assets/armadilha_hd.png", "armadilha_hd.png",
                "assets/hd/armadilha.png", "assets/armadilha.png", "armadilha.png",
                "assets/hd/trap_hd.png", "assets/trap_hd.png", "trap_hd.png",
                "assets/hd/trap.png", "assets/trap.png", "trap.png");
        imagemSkinQuico = carregarImagem(
                "assets/hd/skin_quico_hd.png", "assets/skin_quico_hd.png", "skin_quico_hd.png",
                "assets/skin_quico.png", "skin_quico.png",
                "assets/skin_quico_placeholder.png", "skin_quico_placeholder.png");
        tileWallUnico = carregarImagem(
                "assets/hd/wall_tile_hd.png", "assets/hd/wall_tile.png",
                "assets/hd/divisoria_hd.png", "assets/hd/divisoria.png",
                "assets/wall_tile_hd.png", "assets/wall_tile.png",
                "assets/divisoria_hd.png", "assets/divisoria.png",
                "wall_tile_hd.png", "wall_tile.png",
                "divisoria_hd.png", "divisoria.png");
        tileWallCenter = carregarImagem(
                "assets/hd/wall_center_hd.png", "assets/wall_center_hd.png", "wall_center_hd.png",
                "assets/wall_center.png", "wall_center.png");
        tileWallEdgeTop = carregarImagem(
                "assets/hd/wall_edge_top_hd.png", "assets/wall_edge_top_hd.png", "wall_edge_top_hd.png",
                "assets/wall_edge_top.png", "wall_edge_top.png");
        tileWallEdgeBottom = carregarImagem(
                "assets/hd/wall_edge_bottom_hd.png", "assets/wall_edge_bottom_hd.png", "wall_edge_bottom_hd.png",
                "assets/wall_edge_bottom.png", "wall_edge_bottom.png");
        tileWallEdgeLeft = carregarImagem(
                "assets/hd/wall_edge_left_hd.png", "assets/wall_edge_left_hd.png", "wall_edge_left_hd.png",
                "assets/wall_edge_left.png", "wall_edge_left.png");
        tileWallEdgeRight = carregarImagem(
                "assets/hd/wall_edge_right_hd.png", "assets/wall_edge_right_hd.png", "wall_edge_right_hd.png",
                "assets/wall_edge_right.png", "wall_edge_right.png");
        tileWallCornerTL = carregarImagem(
                "assets/hd/wall_corner_tl_hd.png", "assets/wall_corner_tl_hd.png", "wall_corner_tl_hd.png",
                "assets/wall_corner_tl.png", "wall_corner_tl.png");
        tileWallCornerTR = carregarImagem(
                "assets/hd/wall_corner_tr_hd.png", "assets/wall_corner_tr_hd.png", "wall_corner_tr_hd.png",
                "assets/wall_corner_tr.png", "wall_corner_tr.png");
        tileWallCornerBL = carregarImagem(
                "assets/hd/wall_corner_bl_hd.png", "assets/wall_corner_bl_hd.png", "wall_corner_bl_hd.png",
                "assets/wall_corner_bl.png", "wall_corner_bl.png");
        tileWallCornerBR = carregarImagem(
                "assets/hd/wall_corner_br_hd.png", "assets/wall_corner_br_hd.png", "wall_corner_br_hd.png",
                "assets/wall_corner_br.png", "wall_corner_br.png");

        if (imagemBarriga == null) {
            imagemBarriga = criarJogadorPlaceholder();
        }
        if (imagemPowerVelocidade == null) {
            imagemPowerVelocidade = criarPowerUpPlaceholder(PowerUpTipo.VELOCIDADE);
        }
        if (imagemPowerInvencivel == null) {
            imagemPowerInvencivel = criarPowerUpPlaceholder(PowerUpTipo.INVENCIVEL);
        }
        if (imagemPowerFome == null) {
            imagemPowerFome = criarPowerUpPlaceholder(PowerUpTipo.REGEN_FOME);
        }
        if (imagemPowerPistola == null) {
            imagemPowerPistola = criarPowerUpPlaceholder(PowerUpTipo.PISTOLA);
        }
        if (imagemPowerVida == null) {
            imagemPowerVida = criarPowerUpPlaceholder(PowerUpTipo.VIDA);
        }
        if (imagemCoracao == null) {
            imagemCoracao = criarCoracaoPlaceholder();
        }
        if (imagemSkinQuico == null) {
            imagemSkinQuico = criarSkinQuicoPlaceholder();
        }
        if (imagemFundo == null) {
            imagemFundo = criarFundoHD();
        }
        prepararTilesParedes();

        framesAndar = carregarFramesPorDirecao("seu_barriga_walk_", 2);
        framesCorrer = carregarFramesPorDirecao("seu_barriga_run_", 2);
        framesParado = carregarFramesPorDirecao("seu_barriga_idle_", 2);
        framesDano = carregarFramesPorDirecao("seu_barriga_hurt_", 2);

        if (framesParado == null) {
            framesParado = gerarFramesPadrao(imagemBarriga, Animacao.PARADO);
        }
        if (framesAndar == null) {
            framesAndar = gerarFramesPadrao(imagemBarriga, Animacao.ANDAR);
        }
        if (framesCorrer == null) {
            framesCorrer = gerarFramesPadrao(imagemBarriga, Animacao.CORRER);
        }
        if (framesDano == null) {
            framesDano = gerarFramesPadrao(imagemBarriga, Animacao.DANO);
        }

        framesParadoDefault = framesParado;
        framesAndarDefault = framesAndar;
        framesCorrerDefault = framesCorrer;
        framesDanoDefault = framesDano;

        framesParadoSkin = gerarFramesPadrao(imagemSkinQuico, Animacao.PARADO);
        framesAndarSkin = gerarFramesPadrao(imagemSkinQuico, Animacao.ANDAR);
        framesCorrerSkin = gerarFramesPadrao(imagemSkinQuico, Animacao.CORRER);
        framesDanoSkin = gerarFramesPadrao(imagemSkinQuico, Animacao.DANO);

        atualizarFramesJogador();

        carregarImagensInimigos();
    }

    private BufferedImage carregarImagem(String... caminhos) {
        for (String caminho : caminhos) {
            if (caminho == null) {
                continue;
            }
            File f = new File(caminho);
            if (f.exists()) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        return img;
                    }
                } catch (IOException e) {
                    continue;
                }
            }
        }
        return null;
    }

    private void carregarImagensInimigos() {
        imagemInimigos.clear();
        BufferedImage lento = carregarImagem(
                "assets/hd/inimigo_lento_hd.png", "assets/inimigo_lento_hd.png", "inimigo_lento_hd.png",
                "assets/inimigo_lento.png", "inimigo_lento.png");
        BufferedImage rapido = carregarImagem(
                "assets/hd/inimigo_rapido_hd.png", "assets/inimigo_rapido_hd.png", "inimigo_rapido_hd.png",
                "assets/inimigo_rapido.png", "inimigo_rapido.png");
        BufferedImage guardiao = carregarImagem(
                "assets/hd/inimigo_guardiao_hd.png", "assets/inimigo_guardiao_hd.png", "inimigo_guardiao_hd.png",
                "assets/inimigo_guardiao.png", "inimigo_guardiao.png");
        BufferedImage cacador = carregarImagem(
                "assets/hd/inimigo_cacador_hd.png", "assets/inimigo_cacador_hd.png", "inimigo_cacador_hd.png",
                "assets/inimigo_cacador.png", "inimigo_cacador.png");

        imagemInimigos.put(InimigoTipo.LENTO, lento != null ? lento : criarInimigoPlaceholder(InimigoTipo.LENTO));
        imagemInimigos.put(InimigoTipo.RAPIDO, rapido != null ? rapido : criarInimigoPlaceholder(InimigoTipo.RAPIDO));
        imagemInimigos.put(InimigoTipo.GUARDIAO, guardiao != null ? guardiao : criarInimigoPlaceholder(InimigoTipo.GUARDIAO));
        imagemInimigos.put(InimigoTipo.CACADOR, cacador != null ? cacador : criarInimigoPlaceholder(InimigoTipo.CACADOR));
    }

    private void atualizarFramesJogador() {
        BufferedImage[][] parado = skinQuicoComprada ? framesParadoSkin : framesParadoDefault;
        BufferedImage[][] andar = skinQuicoComprada ? framesAndarSkin : framesAndarDefault;
        BufferedImage[][] correr = skinQuicoComprada ? framesCorrerSkin : framesCorrerDefault;
        BufferedImage[][] dano = skinQuicoComprada ? framesDanoSkin : framesDanoDefault;

        if (parado == null) {
            parado = framesParadoDefault != null ? framesParadoDefault : gerarFramesPadrao(imagemBarriga, Animacao.PARADO);
        }
        if (andar == null) {
            andar = framesAndarDefault != null ? framesAndarDefault : gerarFramesPadrao(imagemBarriga, Animacao.ANDAR);
        }
        if (correr == null) {
            correr = framesCorrerDefault != null ? framesCorrerDefault : gerarFramesPadrao(imagemBarriga, Animacao.CORRER);
        }
        if (dano == null) {
            dano = framesDanoDefault != null ? framesDanoDefault : gerarFramesPadrao(imagemBarriga, Animacao.DANO);
        }

        seuBarriga.definirFrames(parado, andar, correr, dano);
    }

    private BufferedImage[][] carregarFramesPorDirecao(String prefixo, int framesPorDirecao) {
        String[] nomes = new String[] { "up", "down", "left", "right" };
        BufferedImage[][] frames = new BufferedImage[4][framesPorDirecao];
        for (int d = 0; d < nomes.length; d++) {
            for (int i = 0; i < framesPorDirecao; i++) {
                String nome = prefixo + nomes[d] + "_" + i + ".png";
                BufferedImage img = carregarImagem("assets/hd/" + nome, "assets/" + nome, nome);
                if (img == null) {
                    return null;
                }
                frames[d][i] = img;
            }
        }
        return frames;
    }

    private BufferedImage[][] gerarFramesPadrao(BufferedImage base, Animacao animacao) {
        BufferedImage[][] out = new BufferedImage[4][2];
        double[] escalas;
        if (animacao == Animacao.CORRER) {
            escalas = new double[] { 1.06, 0.94 };
        } else if (animacao == Animacao.ANDAR) {
            escalas = new double[] { 1.03, 0.98 };
        } else if (animacao == Animacao.DANO) {
            escalas = new double[] { 1.04, 0.96 };
        } else {
            escalas = new double[] { 1.01, 0.99 };
        }
        Direcao[] dirs = new Direcao[] { Direcao.CIMA, Direcao.BAIXO, Direcao.ESQUERDA, Direcao.DIREITA };
        for (int d = 0; d < dirs.length; d++) {
            for (int i = 0; i < 2; i++) {
                out[d][i] = criarFrame(base, dirs[d], escalas[i], animacao);
            }
        }
        return out;
    }

    private BufferedImage criarFrame(BufferedImage base, Direcao dir, double escala, Animacao animacao) {
        int w = base.getWidth();
        int h = base.getHeight();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.translate(w / 2.0, h / 2.0);
        if (dir == Direcao.ESQUERDA) {
            g.scale(-escala, escala);
        } else {
            g.scale(escala, escala);
        }
        g.translate(-w / 2.0, -h / 2.0);
        g.drawImage(base, 0, 0, null);

        g.dispose();
        return img;
    }

    private BufferedImage criarJogadorPlaceholder() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(50, 100, 220));
        g.fillRoundRect(6, 6, 52, 52, 12, 12);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("SB", 20, 38);
        g.dispose();
        return img;
    }

    private BufferedImage criarFundoHD() {
        int w = BASE_LARGURA;
        int h = BASE_ALTURA;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        GradientPaint base = new GradientPaint(0, 0, new Color(16, 18, 28), 0, h, new Color(10, 12, 18));
        g.setPaint(base);
        g.fillRect(0, 0, w, h);

        Random r = new Random(2026);
        for (int i = 0; i < 8; i++) {
            int cx = r.nextInt(w);
            int cy = r.nextInt(h);
            int raio = 260 + r.nextInt(420);
            Color brilho = new Color(30 + r.nextInt(30), 40 + r.nextInt(30), 60 + r.nextInt(40), 110);
            Color transparente = new Color(brilho.getRed(), brilho.getGreen(), brilho.getBlue(), 0);
            RadialGradientPaint rg = new RadialGradientPaint(
                    new Point2D.Double(cx, cy), raio,
                    new float[] { 0f, 1f }, new Color[] { brilho, transparente });
            g.setPaint(rg);
            g.fillRect(cx - raio, cy - raio, raio * 2, raio * 2);
        }

        g.setComposite(AlphaComposite.SrcOver.derive(0.16f));
        g.setColor(new Color(255, 255, 255, 40));
        for (int x = 0; x < w; x += 120) {
            g.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += 120) {
            g.drawLine(0, y, w, y);
        }
        g.setComposite(AlphaComposite.SrcOver);
        g.dispose();
        return img;
    }

    private BufferedImage criarInimigoPlaceholder(InimigoTipo tipo) {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color base;
        Color borda;
        if (tipo == InimigoTipo.LENTO) {
            base = new Color(200, 70, 70);
            borda = new Color(120, 30, 30);
        } else if (tipo == InimigoTipo.RAPIDO) {
            base = new Color(240, 120, 50);
            borda = new Color(160, 80, 30);
        } else if (tipo == InimigoTipo.GUARDIAO) {
            base = new Color(90, 160, 210);
            borda = new Color(40, 90, 140);
        } else {
            base = new Color(170, 90, 200);
            borda = new Color(90, 40, 120);
        }
        g.setColor(base);
        g.fillOval(6, 6, 52, 52);
        g.setColor(borda);
        g.drawOval(6, 6, 52, 52);
        g.setColor(Color.WHITE);
        g.fillOval(22, 24, 8, 8);
        g.fillOval(34, 24, 8, 8);
        g.setColor(Color.BLACK);
        g.fillOval(25, 26, 4, 4);
        g.fillOval(37, 26, 4, 4);
        g.dispose();
        return img;
    }

    private BufferedImage criarPowerUpPlaceholder(PowerUpTipo tipo) {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color base;
        String texto;
        if (tipo == PowerUpTipo.VELOCIDADE) {
            base = new Color(80, 150, 240);
            texto = "V+";
        } else if (tipo == PowerUpTipo.INVENCIVEL) {
            base = new Color(240, 200, 80);
            texto = "I";
        } else if (tipo == PowerUpTipo.PISTOLA) {
            base = new Color(160, 160, 170);
            texto = "P";
        } else if (tipo == PowerUpTipo.VIDA) {
            base = new Color(230, 90, 90);
            texto = "+";
        } else {
            base = new Color(120, 220, 120);
            texto = "+F";
        }
        g.setColor(base);
        g.fillOval(6, 6, 52, 52);
        g.setColor(new Color(30, 30, 30));
        g.drawOval(6, 6, 52, 52);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        g.drawString(texto, 20, 38);
        g.dispose();
        return img;
    }

    private BufferedImage criarCoracaoPlaceholder() {
        BufferedImage img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(220, 60, 60));
        g.fillOval(2, 2, 8, 8);
        g.fillOval(10, 2, 8, 8);
        Polygon p = new Polygon();
        p.addPoint(1, 7);
        p.addPoint(19, 7);
        p.addPoint(10, 18);
        g.fillPolygon(p);
        g.dispose();
        return img;
    }

    private BufferedImage criarSkinQuicoPlaceholder() {
        int size = 96;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, new Color(30, 40, 60), 0, size, new Color(18, 20, 28));
        g.setPaint(gp);
        g.fillRect(0, 0, size, size);
        g.setColor(new Color(255, 205, 120));
        int face = (int) (size * 0.72);
        int offset = (size - face) / 2;
        g.fillRoundRect(offset, offset, face, face, 20, 20);
        g.setColor(new Color(90, 40, 20));
        g.fillOval(offset + 12, offset + 24, 16, 16);
        g.fillOval(offset + face - 28, offset + 24, 16, 16);
        g.setStroke(new BasicStroke(3f));
        g.drawArc(offset + 22, offset + 40, face - 44, face / 2, 0, -180);
        g.dispose();
        return img;
    }

    private BufferedImage ajustarTile(BufferedImage base, int maxDim) {
        if (base == null) {
            return null;
        }
        int w = base.getWidth();
        int h = base.getHeight();
        int maior = Math.max(w, h);
        if (maior <= maxDim) {
            return base;
        }
        double escala = maxDim / (double) maior;
        int tw = Math.max(1, (int) Math.round(w * escala));
        int th = Math.max(1, (int) Math.round(h * escala));
        BufferedImage img = new BufferedImage(tw, th, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(base, 0, 0, tw, th, null);
        g.dispose();
        return img;
    }

    private void criarTexturas() {
        int maxTile = 192;
        BufferedImage chao = imagemChaoTile != null ? ajustarTile(imagemChaoTile, maxTile) : criarTexturaChao();
        BufferedImage parede = imagemParedeTile != null ? ajustarTile(imagemParedeTile, maxTile) : criarTexturaParede();
        texturaChao = new TexturePaint(chao, new Rectangle(0, 0, chao.getWidth(), chao.getHeight()));
        texturaParede = new TexturePaint(parede, new Rectangle(0, 0, parede.getWidth(), parede.getHeight()));
        prepararTilesChao();
    }

    private void prepararTilesParedes() {
        if (tileWallUnico != null) {
            BufferedImage base = escalarParaTamanho(tileWallUnico, TILE_SIZE, TILE_SIZE);
            tileWallCenter = base;
            tileWallEdgeTop = base;
            tileWallEdgeBottom = base;
            tileWallEdgeLeft = base;
            tileWallEdgeRight = base;
            tileWallCornerTL = base;
            tileWallCornerTR = base;
            tileWallCornerBL = base;
            tileWallCornerBR = base;
            return;
        }
        tileWallCenter = garantirTileParede(tileWallCenter, false, false, false, false, 101);
        tileWallEdgeTop = garantirTileParede(tileWallEdgeTop, true, false, false, false, 102);
        tileWallEdgeBottom = garantirTileParede(tileWallEdgeBottom, false, true, false, false, 103);
        tileWallEdgeLeft = garantirTileParede(tileWallEdgeLeft, false, false, true, false, 104);
        tileWallEdgeRight = garantirTileParede(tileWallEdgeRight, false, false, false, true, 105);
        tileWallCornerTL = garantirTileParede(tileWallCornerTL, true, false, true, false, 106);
        tileWallCornerTR = garantirTileParede(tileWallCornerTR, true, false, false, true, 107);
        tileWallCornerBL = garantirTileParede(tileWallCornerBL, false, true, true, false, 108);
        tileWallCornerBR = garantirTileParede(tileWallCornerBR, false, true, false, true, 109);
    }

    private void prepararTilesChao() {
        BufferedImage base = imagemChaoTile != null
                ? escalarParaTamanho(imagemChaoTile, TILE_SIZE, TILE_SIZE)
                : escalarParaTamanho(criarTexturaChao(), TILE_SIZE, TILE_SIZE);
        tilesChaoVariacoes = new BufferedImage[3];
        tilesChaoVariacoes[0] = base;
        tilesChaoVariacoes[1] = variarTileChao(base, 1.05f, 22);
        tilesChaoVariacoes[2] = variarTileChao(base, 0.95f, 23);
    }

    private BufferedImage garantirTileParede(BufferedImage img, boolean top, boolean bottom, boolean left, boolean right, int seed) {
        if (img != null) {
            return escalarParaTamanho(img, TILE_SIZE, TILE_SIZE);
        }
        return criarTileParedeProcedural(top, bottom, left, right, seed);
    }

    private BufferedImage escalarParaTamanho(BufferedImage img, int w, int h) {
        if (img == null) {
            return null;
        }
        if (img.getWidth() == w && img.getHeight() == h) {
            return img;
        }
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    private BufferedImage variarTileChao(BufferedImage base, float brilho, int seed) {
        int w = base.getWidth();
        int h = base.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Random r = new Random(seed);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = base.getRGB(x, y);
                int a = (argb >> 24) & 0xff;
                int rr = (argb >> 16) & 0xff;
                int gg = (argb >> 8) & 0xff;
                int bb = argb & 0xff;
                int noise = r.nextInt(9) - 4;
                rr = clampInt((int) (rr * brilho) + noise, 0, 255);
                gg = clampInt((int) (gg * brilho) + noise, 0, 255);
                bb = clampInt((int) (bb * brilho) + noise, 0, 255);
                out.setRGB(x, y, (a << 24) | (rr << 16) | (gg << 8) | bb);
            }
        }
        return out;
    }

    private BufferedImage criarTileParedeProcedural(boolean top, boolean bottom, boolean left, boolean right, int seed) {
        int s = TILE_SIZE;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(90, 92, 104));
        g.fillRect(0, 0, s, s);
        Random r = new Random(seed);
        for (int i = 0; i < 120; i++) {
            int x = r.nextInt(s);
            int y = r.nextInt(s);
            int a = 20 + r.nextInt(40);
            g.setColor(new Color(0, 0, 0, a));
            g.fillRect(x, y, 1, 1);
        }
        int edge = Math.max(3, s / 10);
        g.setColor(new Color(60, 62, 72));
        if (top) {
            g.fillRect(0, 0, s, edge);
        }
        if (bottom) {
            g.fillRect(0, s - edge, s, edge);
        }
        if (left) {
            g.fillRect(0, 0, edge, s);
        }
        if (right) {
            g.fillRect(s - edge, 0, edge, s);
        }
        g.setColor(new Color(120, 122, 132, 140));
        g.drawRect(1, 1, s - 3, s - 3);
        g.dispose();
        return img;
    }

    private int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void configurarFontes() {
        String familia = escolherFamiliaFonte(
                "Segoe UI Variable", "Segoe UI", "Bahnschrift", "Trebuchet MS", "Verdana", "SansSerif");
        fonteTitulo = new Font(familia, Font.BOLD, 36);
        fonteSubtitulo = new Font(familia, Font.PLAIN, 18);
        fonteUI = new Font(familia, Font.BOLD, 18);
        fonteHUD = new Font(familia, Font.BOLD, 18);
        fonteHUDMini = new Font(familia, Font.PLAIN, 14);
    }

    private String escolherFamiliaFonte(String... preferidas) {
        String[] disponiveis = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String pref : preferidas) {
            for (String disp : disponiveis) {
                if (disp.equalsIgnoreCase(pref)) {
                    return disp;
                }
            }
        }
        return "SansSerif";
    }

    private BufferedImage criarTexturaChao() {
        int s = 64;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(22, 24, 30));
        g.fillRect(0, 0, s, s);
        g.setColor(new Color(30, 32, 38));
        g.drawLine(0, 0, s - 1, 0);
        g.drawLine(0, s / 2, s - 1, s / 2);
        g.drawLine(0, 0, 0, s - 1);
        g.drawLine(s / 2, 0, s / 2, s - 1);
        Random r = new Random(1337);
        for (int i = 0; i < 140; i++) {
            int x = r.nextInt(s);
            int y = r.nextInt(s);
            int a = 20 + r.nextInt(40);
            g.setColor(new Color(255, 255, 255, a));
            g.fillRect(x, y, 1, 1);
        }
        g.dispose();
        return img;
    }

    private BufferedImage criarTexturaParede() {
        int w = 64;
        int h = 48;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(80, 82, 92));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(60, 62, 72));
        for (int y = 0; y <= h; y += 12) {
            g.drawLine(0, y, w, y);
        }
        for (int y = 0; y < h; y += 12) {
            int offset = (y / 12) % 2 == 0 ? 0 : 16;
            for (int x = offset; x < w; x += 32) {
                g.drawLine(x, y, x, y + 12);
            }
        }
        Random r = new Random(42);
        for (int i = 0; i < 80; i++) {
            int x = r.nextInt(w);
            int y = r.nextInt(h);
            int a = 25 + r.nextInt(50);
            g.setColor(new Color(0, 0, 0, a));
            g.fillRect(x, y, 1, 1);
        }
        g.dispose();
        return img;
    }

    private void configurarTeclado() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        cima = true;
                        break;
                    case KeyEvent.VK_S:
                        baixo = true;
                        break;
                    case KeyEvent.VK_A:
                        esquerda = true;
                        break;
                    case KeyEvent.VK_D:
                        direita = true;
                        break;
                    case KeyEvent.VK_SHIFT:
                        correr = true;
                        break;
                    case KeyEvent.VK_ENTER:
                        if (estado == EstadoJogo.PAUSADO) {
                            retomarJogo();
                        } else if (estado == EstadoJogo.MENU || estado == EstadoJogo.GAME_OVER || estado == EstadoJogo.LOJA) {
                            iniciarTransicao(EstadoJogo.JOGANDO);
                        }
                        break;
                    case KeyEvent.VK_SPACE:
                        if (estado == EstadoJogo.JOGANDO) {
                            atirarPistola();
                        } else if (estado == EstadoJogo.PAUSADO) {
                            retomarJogo();
                        } else if (estado == EstadoJogo.MENU || estado == EstadoJogo.GAME_OVER || estado == EstadoJogo.LOJA) {
                            iniciarTransicao(EstadoJogo.JOGANDO);
                        }
                        break;
                    case KeyEvent.VK_L:
                        if (estado == EstadoJogo.GAME_OVER) {
                            iniciarTransicao(EstadoJogo.LOJA);
                        }
                        break;
                    case KeyEvent.VK_ESCAPE:
                        if (estado == EstadoJogo.LOJA) {
                            iniciarTransicao(EstadoJogo.GAME_OVER);
                        } else if (estado == EstadoJogo.JOGANDO) {
                            entrarPausa();
                        } else if (estado == EstadoJogo.PAUSADO) {
                            retomarJogo();
                        } else if (estado == EstadoJogo.MENU || estado == EstadoJogo.GAME_OVER) {
                            System.exit(0);
                        }
                        break;
                    case KeyEvent.VK_1:
                        if (estado == EstadoJogo.LOJA) {
                            comprarUpgradePorIndice(0);
                        }
                        break;
                    case KeyEvent.VK_2:
                        if (estado == EstadoJogo.LOJA) {
                            comprarUpgradePorIndice(1);
                        }
                        break;
                    case KeyEvent.VK_3:
                        if (estado == EstadoJogo.LOJA) {
                            comprarUpgradePorIndice(2);
                        }
                        break;
                    case KeyEvent.VK_4:
                        if (estado == EstadoJogo.LOJA) {
                            comprarUpgradePorIndice(3);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        cima = false;
                        break;
                    case KeyEvent.VK_S:
                        baixo = false;
                        break;
                    case KeyEvent.VK_A:
                        esquerda = false;
                        break;
                    case KeyEvent.VK_D:
                        direita = false;
                        break;
                    case KeyEvent.VK_SHIFT:
                        correr = false;
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void configurarMouse() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (opcoesVisiveis) {
                    if (botaoVolumeMais.contains(p)) {
                        contadorSegredoVolume = 0;
                        audio.ajustarVolume(0.1);
                        return;
                    }
                    if (botaoVolumeMenos.contains(p)) {
                        processarCliqueVolumeMenos();
                        return;
                    }
                    if (botaoFecharOpcoes.contains(p)) {
                        fecharOpcoes();
                        return;
                    }
                    if (!painelOpcoesBounds.contains(p)) {
                        fecharOpcoes();
                    }
                    return;
                }
                if (estado == EstadoJogo.MENU) {
                    if (botaoIniciar.contains(p)) {
                        iniciarTransicao(EstadoJogo.JOGANDO);
                        requestFocusInWindow();
                        return;
                    } else if (botaoOpcoesMenu.contains(p)) {
                        opcoesVisiveis = true;
                        return;
                    } else if (botaoSairMenu.contains(p)) {
                        System.exit(0);
                        return;
                    }
                }
                if (estado == EstadoJogo.GAME_OVER) {
                    if (botaoIniciar.contains(p)) {
                        iniciarTransicao(EstadoJogo.JOGANDO);
                        requestFocusInWindow();
                    } else if (botaoLoja.contains(p)) {
                        iniciarTransicao(EstadoJogo.LOJA);
                        requestFocusInWindow();
                    } else if (botaoOpcoesGameOver.contains(p)) {
                        opcoesVisiveis = true;
                    } else if (botaoSairGameOver.contains(p)) {
                        System.exit(0);
                    }
                    return;
                }
                if (estado == EstadoJogo.PAUSADO) {
                    if (botaoOpcoesPausa.contains(p)) {
                        opcoesVisiveis = true;
                        return;
                    }
                    processarCliquePausa(p);
                    return;
                }
                if (estado == EstadoJogo.LOJA) {
                    processarCliqueLoja(p);
                }
            }
        });
    }
    private void gameLoop() {
        long agoraNs = System.nanoTime();
        double dt = (agoraNs - ultimoTempoNs) / 1_000_000_000.0;
        ultimoTempoNs = agoraNs;

        if (dt > 0.05) {
            dt = 0.05;
        }

        atualizar(dt);
        repaint();
    }

    private void atualizar(double dt) {
        atualizarTransicao(dt);
        if (estado == EstadoJogo.JOGANDO) {
            atualizarJogo(dt);
        }
    }

    private void atualizarTransicao(double dt) {
        if (fadeDirecao == 0) {
            return;
        }
        double passo = dt / (TRANSICAO_MS / 1000.0);
        fadeAlpha += fadeDirecao * passo;
        if (fadeDirecao > 0 && fadeAlpha >= 1f) {
            fadeAlpha = 1f;
            estado = estadoAlvo;
            if (estado == EstadoJogo.JOGANDO) {
                resetarJogo();
                requestFocusInWindow();
            }
            fadeDirecao = -1;
        } else if (fadeDirecao < 0 && fadeAlpha <= 0f) {
            fadeAlpha = 0f;
            fadeDirecao = 0;
        }
    }

    private void atualizarJogo(double dt) {
        long agoraMs = System.currentTimeMillis();
        verificarSpawnInimigoExtra(agoraMs);
        atualizarPowerUpEstado(agoraMs);

        boolean lentoAtivo = agoraMs < lentoAteMs;
        boolean confuso = agoraMs < confusaoAteMs;
        boolean powerVelocidade = powerUpAtivo == PowerUpTipo.VELOCIDADE && agoraMs < powerUpAteMs;
        boolean powerRegen = powerUpAtivo == PowerUpTipo.REGEN_FOME && agoraMs < powerUpAteMs;
        boolean invencivel = agoraMs < invencivelAteMs || (powerUpAtivo == PowerUpTipo.INVENCIVEL && agoraMs < powerUpAteMs);

        double dx = 0.0;
        double dy = 0.0;
        if (cima) {
            dy -= 1.0;
        }
        if (baixo) {
            dy += 1.0;
        }
        if (esquerda) {
            dx -= 1.0;
        }
        if (direita) {
            dx += 1.0;
        }
        if (confuso) {
            dx = -dx;
            dy = -dy;
        }

        boolean movendo = dx != 0 || dy != 0;
        if (movendo) {
            double len = Math.hypot(dx, dy);
            dx /= len;
            dy /= len;
            seuBarriga.direcao = direcaoPorVetor(dx, dy);
        }

        boolean tentandoCorrer = correr && !lentoAtivo && !staminaExausta && staminaAtual > 0.1;
        double multVel = getMultiplicadorVelocidade();
        double velocidadeAndar = VELOCIDADE_ANDAR * multVel;
        double velocidadeCorrer = VELOCIDADE_CORRER * multVel;
        double velocidadeLenta = VELOCIDADE_LENTA * multVel;
        double velocidadeAtual = velocidadeAndar;
        if (lentoAtivo) {
            velocidadeAtual = velocidadeLenta;
        } else if (tentandoCorrer) {
            velocidadeAtual = velocidadeCorrer;
        }
        if (powerVelocidade) {
            velocidadeAtual *= 1.35;
        }
        if (fomeAtual <= fomeMax * FOME_CRITICA_RATIO) {
            velocidadeAtual *= 0.93;
        }

        double vx = dx * velocidadeAtual;
        double vy = dy * velocidadeAtual;

        boolean escorregadioAntes = emEscorregadio;
        if (escorregadioAntes) {
            seuBarriga.deslizeX = lerp(seuBarriga.deslizeX, vx, 0.08);
            seuBarriga.deslizeY = lerp(seuBarriga.deslizeY, vy, 0.08);
            vx = seuBarriga.deslizeX;
            vy = seuBarriga.deslizeY;
        } else {
            seuBarriga.deslizeX = vx;
            seuBarriga.deslizeY = vy;
        }

        moverComColisao(vx * dt, 0);
        moverComColisao(0, vy * dt);

        seuBarriga.correndo = tentandoCorrer;

        Animacao animacao = Animacao.PARADO;
        if (agoraMs < danoAteMs) {
            animacao = Animacao.DANO;
        } else if (movendo) {
            animacao = tentandoCorrer ? Animacao.CORRER : Animacao.ANDAR;
        }
        seuBarriga.animacaoAtual = animacao;
        seuBarriga.atualizarAnimacao(dt);

        atualizarPerigos(agoraMs);
        aplicarPerigosNoJogador(agoraMs);
        atualizarRecursos(dt, agoraMs, tentandoCorrer, powerRegen);

        atualizarInimigos(dt, agoraMs);
        verificarColetaCroissant();
        verificarColetaPowerUp();
        verificarColetaItemEspecial();
        verificarColisaoInimigos(agoraMs, invencivel);

        atualizarMissao(agoraMs, dt);
        atualizarDificuldade();
        atualizarSpawns(agoraMs);

        animCroissantTempo += dt;
        animPowerUpTempo += dt;

        atualizarEfeitos(agoraMs);
        atualizarParticulas(dt);
    }

    private void atualizarPowerUpEstado(long agoraMs) {
        if (powerUpAtivo != null && agoraMs >= powerUpAteMs) {
            powerUpAtivo = null;
        }
    }

    private void atualizarRecursos(double dt, long agoraMs, boolean correndoAgora, boolean powerRegen) {
        double fomeDreno = FOME_DRENO_BASE + (correndoAgora ? FOME_DRENO_CORRER : 0);
        if (emZonaFome) {
            fomeDreno += FOME_DRENO_ZONA;
        }
        fomeAtual -= fomeDreno * dt;
        if (powerRegen) {
            fomeAtual += FOME_REGEN_POWERUP * dt;
        }
        fomeAtual = clamp(fomeAtual, 0, fomeMax);

        if (fomeAtual <= 0 && agoraMs >= proximoDanoFomeMs && agoraMs >= invencivelAteMs) {
            aplicarDano(agoraMs, 1, 0, 0, false, false);
            proximoDanoFomeMs = agoraMs + FOME_DANO_INTERVALO_MS;
        }

        if (correndoAgora) {
            staminaAtual -= STAMINA_CUSTO_CORRER * dt;
        } else {
            staminaAtual += STAMINA_REGEN * dt;
        }
        staminaAtual = clamp(staminaAtual, 0, staminaMax);
        if (staminaAtual <= 0) {
            staminaExausta = true;
        } else if (staminaExausta && staminaAtual >= staminaMax * STAMINA_RETORNO_LIMIAR) {
            staminaExausta = false;
        }
    }

    private void atualizarPerigos(long agoraMs) {
        for (Perigo p : perigos) {
            p.atualizar(agoraMs);
        }
    }

    private void aplicarPerigosNoJogador(long agoraMs) {
        emEscorregadio = false;
        emZonaFome = false;
        Rectangle2D player = seuBarriga.getBounds();
        for (Perigo p : perigos) {
            if (!p.getBounds().intersects(player)) {
                continue;
            }
            switch (p.tipo) {
                case ESCORREGADIO:
                    emEscorregadio = true;
                    break;
                case ZONA_FOME:
                    emZonaFome = true;
                    break;
                case BURACO:
                    if (agoraMs >= invencivelAteMs) {
                        aplicarDano(agoraMs, 1, 10, 0, false, false);
                        reposicionarJogadorParaAreaSegura();
                    }
                    break;
                case ARMADILHA:
                    if (p.ativo && agoraMs >= invencivelAteMs) {
                        aplicarDano(agoraMs, 1, 6, 6, true, false);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void reposicionarJogadorParaAreaSegura() {
        if (areaSpawn != null) {
            seuBarriga.x = areaSpawn.getCenterX() - seuBarriga.tamanho / 2.0;
            seuBarriga.y = areaSpawn.getCenterY() - seuBarriga.tamanho / 2.0;
        } else {
            seuBarriga.x = 40;
            seuBarriga.y = 40;
        }
    }

    private void atualizarInimigos(double dt, long agoraMs) {
        double multiplicador = 1.0 + (nivelDificuldade - 1) * 0.12;
        double px = seuBarriga.x + seuBarriga.tamanho / 2.0;
        double py = seuBarriga.y + seuBarriga.tamanho / 2.0;
        boolean playerCorrendo = seuBarriga.correndo;

        for (Inimigo inimigo : inimigos) {
            double velocidade = inimigo.velocidadeBase * multiplicador;
            if (agoraMs < inimigo.lentoAteMs) {
                velocidade *= INIMIGO_LENTO_DANO_MULT;
            }
            double dist = Math.hypot(px - inimigo.x, py - inimigo.y);
            double ajusteVel = 1.0;
            if (inimigo.tipo == InimigoTipo.RAPIDO && dist < 90) {
                ajusteVel = 0.6 + 0.4 * (dist / 90.0);
            }
            velocidade *= ajusteVel;
            double raio = RAIO_DETECCAO_BASE + (playerCorrendo ? RAIO_DETECCAO_CORRER : 0);

            if (inimigo.tipo == InimigoTipo.RAPIDO) {
                raio *= 1.1;
            }
            if (inimigo.tipo == InimigoTipo.GUARDIAO) {
                raio *= 0.8;
            }

            boolean perseguindo = false;
            double alvoX = px;
            double alvoY = py;

            if (inimigo.tipo == InimigoTipo.CACADOR) {
                if (agoraMs < inimigo.perseguirAteMs) {
                    perseguindo = true;
                } else if (agoraMs >= inimigo.proximoCacarMs && dist < raio) {
                    inimigo.perseguirAteMs = agoraMs + CACADOR_PERSEGUIR_MS;
                    inimigo.proximoCacarMs = inimigo.perseguirAteMs + CACADOR_COOLDOWN_MS;
                    perseguindo = true;
                }
            } else if (inimigo.tipo == InimigoTipo.GUARDIAO) {
                double distAncoragem = Math.hypot(inimigo.ancoraX - inimigo.x, inimigo.ancoraY - inimigo.y);
                if (dist < raio && distAncoragem < RAIO_GUARDIAO) {
                    perseguindo = true;
                } else if (distAncoragem > RAIO_GUARDIAO * 1.1) {
                    alvoX = inimigo.ancoraX;
                    alvoY = inimigo.ancoraY;
                    perseguindo = true;
                }
            } else {
                if (dist < raio) {
                    inimigo.perseguirAteMs = agoraMs + PERSEGUIR_COMUM_MS;
                }
                if (agoraMs < inimigo.perseguirAteMs) {
                    perseguindo = true;
                }
            }

            if (perseguindo) {
                double[] dir = direcaoPara(inimigo.x, inimigo.y, alvoX, alvoY);
                moverInimigo(inimigo, dir[0] * velocidade * dt, dir[1] * velocidade * dt);
            } else {
                if (agoraMs >= inimigo.proximaDecisaoMs) {
                    double[] dir = direcaoAleatoria();
                    inimigo.dirX = dir[0];
                    inimigo.dirY = dir[1];
                    inimigo.proximaDecisaoMs = agoraMs + 1200 + rng.nextInt(1200);
                }
                moverInimigo(inimigo, inimigo.dirX * velocidade * dt, inimigo.dirY * velocidade * dt);
            }
        }
    }

    private void verificarSpawnInimigoExtra(long agoraMs) {
        if (jogoInicioMs == 0) {
            jogoInicioMs = agoraMs;
        }
        if (proximoSpawnInimigoExtraMs == 0) {
            proximoSpawnInimigoExtraMs = agoraMs + EXTRA_SPAWN_INICIAL_MS;
        }
        if (agoraMs < proximoSpawnInimigoExtraMs) {
            return;
        }
        double tempoSeg = (agoraMs - jogoInicioMs) / 1000.0;
        tempoSeg = Math.max(0.0, tempoSeg);
        double fase = Math.min(1.0, tempoSeg / EXTRA_SPAWN_CURVE_SEG);
        long intervalo = EXTRA_SPAWN_MIN_MS
                + (long) ((1.0 - fase) * (EXTRA_SPAWN_INICIAL_MS - EXTRA_SPAWN_MIN_MS));
        intervalo = Math.max(EXTRA_SPAWN_MIN_MS, Math.min(EXTRA_SPAWN_INICIAL_MS, intervalo));
        proximoSpawnInimigoExtraMs = agoraMs + intervalo;
        InimigoTipo tipo;
        if (tempoSeg > 50) {
            tipo = InimigoTipo.CACADOR;
        } else if (tempoSeg > 32) {
            tipo = InimigoTipo.RAPIDO;
        } else if (tempoSeg > 18) {
            tipo = InimigoTipo.GUARDIAO;
        } else {
            tipo = InimigoTipo.LENTO;
        }
        spawnInimigo(tipo);
    }

    private void moverInimigo(Inimigo inimigo, double dx, double dy) {
        double nx = inimigo.x + dx;
        double ny = inimigo.y + dy;
        if (podeMoverPara(inimigo.getBounds(nx, ny))) {
            inimigo.x = nx;
            inimigo.y = ny;
            return;
        }
        boolean moveu = false;
        if (podeMoverPara(inimigo.getBounds(nx, inimigo.y))) {
            inimigo.x = nx;
            moveu = true;
        }
        if (podeMoverPara(inimigo.getBounds(inimigo.x, ny))) {
            inimigo.y = ny;
            moveu = true;
        }
        if (!moveu) {
            inimigo.dirX *= -1;
            inimigo.dirY *= -1;
        }
    }

    private double[] direcaoPara(double x, double y, double alvoX, double alvoY) {
        double dx = alvoX - x;
        double dy = alvoY - y;
        double len = Math.hypot(dx, dy);
        if (len == 0) {
            return new double[] { 0, 0 };
        }
        return new double[] { dx / len, dy / len };
    }

    private double[] direcaoAleatoria() {
        double ang = rng.nextDouble() * Math.PI * 2.0;
        return new double[] { Math.cos(ang), Math.sin(ang) };
    }
    private void verificarColetaCroissant() {
        if (croissant == null) {
            return;
        }
        if (seuBarriga.getBounds().intersects(croissant.getBounds())) {
            pontuacao += getBonusForca();
            croissantsColetados += 1;
            fomeAtual = clamp(fomeAtual + getGanhoCroissant(), 0, fomeMax);
            efeitos.add(new Efeito(croissant.x + croissant.tamanho / 2.0, croissant.y + croissant.tamanho / 2.0, 420, new Color(255, 200, 90)));
            criarParticulas(croissant.x + croissant.tamanho / 2.0, croissant.y + croissant.tamanho / 2.0, PARTICULAS_CROISSANT, new Color(255, 200, 90));
            tocarSomCroissant();

            if (missaoAtual != null) {
                if (missaoAtual.tipo == MissaoTipo.CROISSANTS_SEM_DANO || missaoAtual.tipo == MissaoTipo.CROISSANTS_TEMPO) {
                    missaoAtual.progresso += 1;
                } else if (missaoAtual.tipo == MissaoTipo.CROISSANTS_CORRER && seuBarriga.correndo) {
                    missaoAtual.progresso += 1;
                }
            }
            spawnCroissant();
        }
    }

    private void verificarColetaPowerUp() {
        if (powerUpAtual == null) {
            return;
        }
        if (seuBarriga.getBounds().intersects(powerUpAtual.getBounds())) {
            ativarPowerUp(powerUpAtual.tipo);
            Color cor = getCorPowerUp(powerUpAtual.tipo);
            efeitos.add(new Efeito(powerUpAtual.x + powerUpAtual.tamanho / 2.0, powerUpAtual.y + powerUpAtual.tamanho / 2.0, 450, cor));
            criarParticulas(powerUpAtual.x + powerUpAtual.tamanho / 2.0, powerUpAtual.y + powerUpAtual.tamanho / 2.0, 12, cor);
            audio.tocarPowerUp();
            if (missaoAtual != null && missaoAtual.tipo == MissaoTipo.PEGAR_POWERUPS) {
                missaoAtual.progresso += 1;
            }
            powerUpAtual = null;
            proximoSpawnPowerUpMs = System.currentTimeMillis() + getTempoSpawnPowerUpMs();
        }
    }

    private void verificarColetaItemEspecial() {
        if (itemEspecialAtual == null) {
            return;
        }
        if (seuBarriga.getBounds().intersects(itemEspecialAtual.getBounds())) {
            ativarPowerUp(itemEspecialAtual.tipo);
            Color cor = getCorPowerUp(itemEspecialAtual.tipo);
            efeitos.add(new Efeito(itemEspecialAtual.x + itemEspecialAtual.tamanho / 2.0, itemEspecialAtual.y + itemEspecialAtual.tamanho / 2.0, 450, cor));
            criarParticulas(itemEspecialAtual.x + itemEspecialAtual.tamanho / 2.0, itemEspecialAtual.y + itemEspecialAtual.tamanho / 2.0, 12, cor);
            audio.tocarPowerUp();
            if (missaoAtual != null && missaoAtual.tipo == MissaoTipo.PEGAR_POWERUPS) {
                missaoAtual.progresso += 1;
            }
            itemEspecialAtual = null;
            proximoSpawnItemMs = System.currentTimeMillis() + getTempoSpawnItemMs();
        }
    }

    private void verificarColisaoInimigos(long agoraMs, boolean invencivel) {
        if (invencivel) {
            return;
        }
        for (Inimigo inimigo : inimigos) {
            if (seuBarriga.getBounds().intersects(inimigo.getBounds())) {
                aplicarDanoInimigo(inimigo, agoraMs);
                break;
            }
        }
    }

    private void aplicarDanoInimigo(Inimigo inimigo, long agoraMs) {
        inimigo.lentoAteMs = agoraMs + INIMIGO_LENTO_DANO_MS;
        switch (inimigo.tipo) {
            case LENTO:
                aplicarDano(agoraMs, 1, 12, 0, true, false);
                break;
            case RAPIDO:
                aplicarDano(agoraMs, 1, 4, 15, false, false);
                break;
            case GUARDIAO:
                aplicarDano(agoraMs, 1, 8, 8, true, false);
                break;
            case CACADOR:
                aplicarDano(agoraMs, 1, 6, 6, false, true);
                break;
            default:
                aplicarDano(agoraMs, 1, 6, 6, false, false);
                break;
        }
    }

    private void aplicarDano(long agoraMs, int danoVidas, double fomePerdida, double staminaPerdida, boolean aplicarLento, boolean aplicarConfusao) {
        vidas -= danoVidas;
        pontuacao = Math.max(0, pontuacao - 1);
        fomeAtual = clamp(fomeAtual - fomePerdida, 0, fomeMax);
        staminaAtual = clamp(staminaAtual - staminaPerdida, 0, staminaMax);
        audio.tocarDano();
        if (staminaAtual <= 0) {
            staminaExausta = true;
        }
        if (aplicarLento) {
            lentoAteMs = agoraMs + TEMPO_LENTO_MS;
        }
        if (aplicarConfusao) {
            confusaoAteMs = agoraMs + CONFUSAO_MS;
        }
        invencivelAteMs = agoraMs + DANO_INVENCIVEL_MS;
        danoAteMs = Math.max(agoraMs + DANO_PISCA_MS, invencivelAteMs);

        double cx = seuBarriga.x + seuBarriga.tamanho / 2.0;
        double cy = seuBarriga.y + seuBarriga.tamanho / 2.0;
        efeitos.add(new Efeito(cx, cy, 400, new Color(255, 80, 80)));
        criarParticulas(cx, cy, PARTICULAS_DANO, new Color(255, 90, 90));

        if (missaoAtual != null && missaoAtual.tipo == MissaoTipo.CROISSANTS_SEM_DANO) {
            missaoAtual.falhou = true;
        }

        if (vidas <= 0) {
            finalizarJogo();
        }
    }

    private void ativarPowerUp(PowerUpTipo tipo) {
        long agora = System.currentTimeMillis();
        if (tipo == PowerUpTipo.PISTOLA) {
            balasPistola += getBalasPorPowerUpPistola();
            return;
        }
        if (tipo == PowerUpTipo.VIDA) {
            vidas = Math.min(getMaxVidasComSkin(), vidas + 1);
            return;
        }
        powerUpAtivo = tipo;
        powerUpAteMs = agora + getDuracaoPowerUpAtual();
        if (tipo == PowerUpTipo.INVENCIVEL) {
            invencivelAteMs = Math.max(invencivelAteMs, powerUpAteMs);
        }
    }

    private void atualizarMissao(long agoraMs, double dt) {
        if (missaoAtual == null) {
            if (agoraMs >= proximaMissaoMs) {
                missaoAtual = criarMissao(agoraMs);
            }
            return;
        }

        if (missaoAtual.falhou) {
            missaoAtual = null;
            proximaMissaoMs = agoraMs + 2000;
            return;
        }

        switch (missaoAtual.tipo) {
            case CROISSANTS_SEM_DANO:
                if (missaoAtual.progresso >= missaoAtual.alvo) {
                    missaoAtual.concluida = true;
                }
                break;
            case CROISSANTS_TEMPO:
                if (agoraMs - missaoAtual.inicioMs > missaoAtual.duracaoMs) {
                    missaoAtual.falhou = true;
                } else if (missaoAtual.progresso >= missaoAtual.alvo) {
                    missaoAtual.concluida = true;
                }
                break;
            case CROISSANTS_CORRER:
                if (agoraMs - missaoAtual.inicioMs > missaoAtual.duracaoMs) {
                    missaoAtual.falhou = true;
                } else if (missaoAtual.progresso >= missaoAtual.alvo) {
                    missaoAtual.concluida = true;
                }
                break;
            case PEGAR_POWERUPS:
                if (agoraMs - missaoAtual.inicioMs > missaoAtual.duracaoMs) {
                    missaoAtual.falhou = true;
                } else if (missaoAtual.progresso >= missaoAtual.alvo) {
                    missaoAtual.concluida = true;
                }
                break;
            default:
                break;
        }

        if (missaoAtual.concluida) {
            concederRecompensaMissao(agoraMs);
            missaoAtual = null;
            proximaMissaoMs = agoraMs + 2500;
        }
    }

    private Missao criarMissao(long agoraMs) {
        MissaoTipo[] tipos = MissaoTipo.values();
        MissaoTipo tipo = tipos[rng.nextInt(tipos.length)];
        Missao missao = new Missao(tipo);
        missao.inicioMs = agoraMs;

        if (tipo == MissaoTipo.CROISSANTS_SEM_DANO) {
            missao.alvo = 4 + rng.nextInt(3);
            missao.bonusPontos = 7;
        } else if (tipo == MissaoTipo.CROISSANTS_TEMPO) {
            missao.alvo = 3 + rng.nextInt(3);
            missao.duracaoMs = 12000 + rng.nextInt(6000);
            missao.bonusPontos = 8;
        } else if (tipo == MissaoTipo.CROISSANTS_CORRER) {
            missao.alvo = 3 + rng.nextInt(3);
            missao.duracaoMs = 14000 + rng.nextInt(6000);
            missao.bonusPontos = 9;
        } else if (tipo == MissaoTipo.PEGAR_POWERUPS) {
            int alvo = 1 + rng.nextInt(2);
            missao.alvo = alvo;
            missao.duracaoMs = 15000 + alvo * 8000 + rng.nextInt(5000);
            missao.bonusPontos = 10;
        }
        return missao;
    }

    private void concederRecompensaMissao(long agoraMs) {
        int bonus = missaoAtual.bonusPontos;
        pontuacao += bonus;
        pontosLoja += bonus;
        pontosTotais += bonus;
        bonusLojaRecebido += bonus;
        efeitos.add(new Efeito(seuBarriga.x + seuBarriga.tamanho / 2.0, seuBarriga.y + seuBarriga.tamanho / 2.0, 450, new Color(120, 220, 120)));
        if (powerUpAtual == null) {
            spawnPowerUp();
        } else {
            ativarPowerUp(sortearPowerUpTipo());
        }
    }

    private void atualizarDificuldade() {
        if (pontuacao >= proximoNivelEm) {
            nivelDificuldade += 1;
            proximoNivelEm += PONTOS_PARA_NOVO_NIVEL;
        }
        if (pontuacao >= proximoInimigoScore) {
            spawnInimigoAleatorio();
            proximoInimigoScore += PONTOS_PARA_NOVO_INIMIGO;
        }
        audio.atualizarMusica(nivelDificuldade);
    }

    private void atualizarSpawns(long agoraMs) {
        if (croissant == null) {
            spawnCroissant();
        }
        if (powerUpAtual == null && agoraMs >= proximoSpawnPowerUpMs) {
            spawnPowerUp();
            proximoSpawnPowerUpMs = agoraMs + getTempoSpawnPowerUpMs();
        }
        if (itemEspecialAtual == null && agoraMs >= proximoSpawnItemMs) {
            spawnItemEspecial();
            proximoSpawnItemMs = agoraMs + getTempoSpawnItemMs();
        }
    }

    private long getTempoSpawnPowerUpMs() {
        long base = POWERUP_SPAWN_BASE_MS - (nivelDificuldade - 1) * 800;
        base = Math.max(POWERUP_SPAWN_MIN_MS, base);
        return base + rng.nextInt(2000);
    }

    private long getTempoSpawnItemMs() {
        long base = ITEM_SPAWN_BASE_MS - (nivelDificuldade - 1) * 700;
        base = Math.max(ITEM_SPAWN_MIN_MS, base);
        return base + rng.nextInt(2200);
    }

    private void spawnCroissant() {
        int maxX = Math.max(0, largura - TAMANHO_CROISSANT);
        int maxY = Math.max(0, altura - TAMANHO_CROISSANT);

        for (int i = 0; i < MAX_TENTATIVAS_SPAWN; i++) {
            double x = rng.nextInt(maxX + 1);
            double y = rng.nextInt(maxY + 1);
            Croissant candidato = new Croissant(x, y, TAMANHO_CROISSANT);
            if (areaLivre(candidato.getBounds(), 120, true)) {
                croissant = candidato;
                return;
            }
        }
        croissant = new Croissant(60, 60, TAMANHO_CROISSANT);
    }

    private void spawnPowerUp() {
        int maxX = Math.max(0, largura - TAMANHO_POWERUP);
        int maxY = Math.max(0, altura - TAMANHO_POWERUP);

        for (int i = 0; i < MAX_TENTATIVAS_SPAWN; i++) {
            double x = rng.nextInt(maxX + 1);
            double y = rng.nextInt(maxY + 1);
            PowerUp candidato = new PowerUp(x, y, TAMANHO_POWERUP, sortearPowerUpTipo());
            if (!areaLivre(candidato.getBounds(), 160, true)) {
                continue;
            }
            if (croissant != null && candidato.getBounds().intersects(croissant.getBounds())) {
                continue;
            }
            if (itemEspecialAtual != null && candidato.getBounds().intersects(itemEspecialAtual.getBounds())) {
                continue;
            }
            powerUpAtual = candidato;
            return;
        }
    }

    private PowerUpTipo sortearPowerUpTipo() {
        double roll = rng.nextDouble();
        if (roll < 0.38) {
            return PowerUpTipo.VELOCIDADE;
        }
        if (roll < 0.76) {
            return PowerUpTipo.REGEN_FOME;
        }
        return PowerUpTipo.INVENCIVEL;
    }

    private void spawnItemEspecial() {
        int maxX = Math.max(0, largura - TAMANHO_POWERUP);
        int maxY = Math.max(0, altura - TAMANHO_POWERUP);

        for (int i = 0; i < MAX_TENTATIVAS_SPAWN; i++) {
            double x = rng.nextInt(maxX + 1);
            double y = rng.nextInt(maxY + 1);
            PowerUp candidato = new PowerUp(x, y, TAMANHO_POWERUP, sortearItemEspecialTipo());
            if (!areaLivre(candidato.getBounds(), 160, true)) {
                continue;
            }
            if (croissant != null && candidato.getBounds().intersects(croissant.getBounds())) {
                continue;
            }
            if (powerUpAtual != null && candidato.getBounds().intersects(powerUpAtual.getBounds())) {
                continue;
            }
            itemEspecialAtual = candidato;
            return;
        }
    }

    private PowerUpTipo sortearItemEspecialTipo() {
        return rng.nextDouble() < 0.58 ? PowerUpTipo.PISTOLA : PowerUpTipo.VIDA;
    }

    private void spawnInimigoAleatorio() {
        InimigoTipo[] tipos = InimigoTipo.values();
        spawnInimigo(tipos[rng.nextInt(tipos.length)]);
    }

    private void spawnInimigo(InimigoTipo tipo) {
        int tamanho;
        double velocidade;
        if (tipo == InimigoTipo.LENTO) {
            tamanho = INIMIGO_LENTO_TAM;
            velocidade = INIMIGO_LENTO_VEL;
        } else if (tipo == InimigoTipo.RAPIDO) {
            tamanho = INIMIGO_RAPIDO_TAM;
            velocidade = INIMIGO_RAPIDO_VEL;
        } else if (tipo == InimigoTipo.GUARDIAO) {
            tamanho = INIMIGO_GUARDIAO_TAM;
            velocidade = INIMIGO_GUARDIAO_VEL;
        } else {
            tamanho = INIMIGO_CACADOR_TAM;
            velocidade = INIMIGO_CACADOR_VEL;
        }

        int maxX = Math.max(0, largura - tamanho);
        int maxY = Math.max(0, altura - tamanho);

        for (int i = 0; i < MAX_TENTATIVAS_SPAWN; i++) {
            double x = rng.nextInt(maxX + 1);
            double y = rng.nextInt(maxY + 1);
            Inimigo candidato = new Inimigo(x, y, tamanho, tipo, velocidade);
            if (tipo == InimigoTipo.GUARDIAO && !perigos.isEmpty()) {
                Perigo p = perigos.get(rng.nextInt(perigos.size()));
                candidato.ancoraX = p.x + p.w / 2.0;
                candidato.ancoraY = p.y + p.h / 2.0;
                candidato.x = clamp(candidato.ancoraX - tamanho / 2.0 + rng.nextInt(60) - 30, 0, maxX);
                candidato.y = clamp(candidato.ancoraY - tamanho / 2.0 + rng.nextInt(60) - 30, 0, maxY);
            }
            if (areaLivre(candidato.getBounds(), 220, true)) {
                double[] dir = direcaoAleatoria();
                candidato.dirX = dir[0];
                candidato.dirY = dir[1];
                candidato.proximaDecisaoMs = System.currentTimeMillis() + 1200 + rng.nextInt(1200);
                inimigos.add(candidato);
                return;
            }
        }
    }

    private boolean areaLivre(Rectangle2D area, double distanciaMinJogador, boolean evitarPerigos) {
        if (area.getMinX() < 0 || area.getMinY() < 0 || area.getMaxX() > largura || area.getMaxY() > altura) {
            return false;
        }
        if (colideComObstaculos(area)) {
            return false;
        }
        if (evitarPerigos) {
            for (Perigo p : perigos) {
                if (p.getBounds().intersects(area)) {
                    return false;
                }
            }
        }
        for (Inimigo i : inimigos) {
            if (i.getBounds().intersects(area)) {
                return false;
            }
        }
        if (distanciaMinJogador > 0) {
            double cx = area.getCenterX();
            double cy = area.getCenterY();
            double px = seuBarriga.x + seuBarriga.tamanho / 2.0;
            double py = seuBarriga.y + seuBarriga.tamanho / 2.0;
            if (Math.hypot(cx - px, cy - py) < distanciaMinJogador) {
                return false;
            }
        }
        return true;
    }

    private void moverComColisao(double dx, double dy) {
        if (dx == 0 && dy == 0) {
            return;
        }
        double nx = seuBarriga.x + dx;
        double ny = seuBarriga.y + dy;
        Rectangle2D bounds = seuBarriga.getBounds(nx, ny);
        if (!podeMoverPara(bounds)) {
            return;
        }
        seuBarriga.x = nx;
        seuBarriga.y = ny;
    }

    private boolean podeMoverPara(Rectangle2D bounds) {
        if (bounds.getMinX() < 0 || bounds.getMinY() < 0 || bounds.getMaxX() > largura || bounds.getMaxY() > altura) {
            return false;
        }
        return !colideComObstaculos(bounds);
    }

    private boolean colideComObstaculos(Rectangle2D bounds) {
        if (tilesParede == null) {
            for (Obstaculo o : obstaculos) {
                if (o.getBounds().intersects(bounds)) {
                    return true;
                }
            }
            return false;
        }
        int startCol = (int) Math.floor(bounds.getMinX() / TILE_SIZE);
        int endCol = (int) Math.floor((bounds.getMaxX() - 1) / TILE_SIZE);
        int startRow = (int) Math.floor(bounds.getMinY() / TILE_SIZE);
        int endRow = (int) Math.floor((bounds.getMaxY() - 1) / TILE_SIZE);
        startCol = Math.max(0, startCol);
        startRow = Math.max(0, startRow);
        endCol = Math.min(tilesCols - 1, endCol);
        endRow = Math.min(tilesRows - 1, endRow);
        for (int c = startCol; c <= endCol; c++) {
            for (int r = startRow; r <= endRow; r++) {
                if (tilesParede[c][r]) {
                    return true;
                }
            }
        }
        return false;
    }

    private void criarParticulas(double x, double y, int quantidade, Color cor) {
        for (int i = 0; i < quantidade; i++) {
            double ang = rng.nextDouble() * Math.PI * 2.0;
            double vel = 40 + rng.nextDouble() * 90;
            double vx = Math.cos(ang) * vel;
            double vy = Math.sin(ang) * vel;
            particulas.add(new Particula(x, y, vx, vy, 0.5 + rng.nextDouble() * 0.5, cor));
        }
    }

    private void atualizarParticulas(double dt) {
        for (int i = particulas.size() - 1; i >= 0; i--) {
            Particula p = particulas.get(i);
            p.atualizar(dt);
            if (p.vida <= 0) {
                particulas.remove(i);
            }
        }
    }

    private void atualizarEfeitos(long agoraMs) {
        for (int i = efeitos.size() - 1; i >= 0; i--) {
            Efeito e = efeitos.get(i);
            if (agoraMs - e.inicioMs > e.duracaoMs) {
                efeitos.remove(i);
            }
        }
    }

    private void tocarSomCroissant() {
        audio.tocarCroissant();
    }

    private void atirarPistola() {
        if (balasPistola <= 0) {
            return;
        }
        long agoraMs = System.currentTimeMillis();
        if (agoraMs < proximoTiroMs) {
            return;
        }
        double px = seuBarriga.x + seuBarriga.tamanho / 2.0;
        double py = seuBarriga.y + seuBarriga.tamanho / 2.0;
        Inimigo alvo = null;
        double melhorDist = Double.MAX_VALUE;
        for (Inimigo i : inimigos) {
            double ix = i.x + i.tamanho / 2.0;
            double iy = i.y + i.tamanho / 2.0;
            double dist = Math.hypot(px - ix, py - iy);
            if (dist <= PISTOLA_RAIO && dist < melhorDist) {
                melhorDist = dist;
                alvo = i;
            }
        }
        if (alvo == null) {
            return;
        }
        balasPistola -= 1;
        proximoTiroMs = agoraMs + PISTOLA_COOLDOWN_MS;
        inimigos.remove(alvo);
        pontuacao += getBonusForca();

        double cx = alvo.x + alvo.tamanho / 2.0;
        double cy = alvo.y + alvo.tamanho / 2.0;
        efeitos.add(new Efeito(cx, cy, 340, new Color(255, 220, 160)));
        criarParticulas(cx, cy, 12, new Color(255, 210, 160));
        audio.tocarPistola();
    }

    private void resetarJogo() {
        pontuacao = 0;
        vidas = getVidasIniciaisComSkin();
        croissantsColetados = 0;
        bonusLojaRecebido = 0;
        nivelDificuldade = 1;
        proximoNivelEm = PONTOS_PARA_NOVO_NIVEL;
        proximoInimigoScore = PONTOS_PARA_NOVO_INIMIGO;

        aplicarUpgrades();
        fomeAtual = fomeMax;
        staminaAtual = staminaMax;
        staminaExausta = false;

        lentoAteMs = 0L;
        invencivelAteMs = 0L;
        danoAteMs = 0L;
        confusaoAteMs = 0L;
        powerUpAtivo = null;
        powerUpAteMs = 0L;
        balasPistola = 0;
        proximoTiroMs = 0L;
        proximoDanoFomeMs = System.currentTimeMillis() + FOME_DANO_INTERVALO_MS;

        gerarMapa();
        reposicionarJogadorParaAreaSegura();
        seuBarriga.direcao = Direcao.BAIXO;
        seuBarriga.resetAnimacao();

        inimigos.clear();
        efeitos.clear();
        particulas.clear();

        spawnInimigo(InimigoTipo.LENTO);
        spawnInimigo(InimigoTipo.RAPIDO);
        spawnInimigo(InimigoTipo.GUARDIAO);
        spawnInimigo(InimigoTipo.CACADOR);

        spawnCroissant();
        powerUpAtual = null;
        itemEspecialAtual = null;

        long agoraMs = System.currentTimeMillis();
        proximoSpawnPowerUpMs = agoraMs + getTempoSpawnPowerUpMs();
        proximoSpawnItemMs = agoraMs + getTempoSpawnItemMs();
        missaoAtual = null;
        proximaMissaoMs = agoraMs + 2000;

        audio.atualizarMusica(nivelDificuldade);

        cima = baixo = esquerda = direita = correr = false;
        jogoInicioMs = agoraMs;
        proximoSpawnInimigoExtraMs = agoraMs + 4500;
    }

    private void finalizarJogo() {
        if (estado != EstadoJogo.JOGANDO) {
            return;
        }
        int pontosPartida = Math.max(0, pontuacao - bonusLojaRecebido);
        melhorPontuacao = Math.max(melhorPontuacao, pontuacao);
        pontosLoja += pontosPartida;
        pontosTotais += pontosPartida;
        audio.tocarGameOver();
        iniciarTransicao(EstadoJogo.GAME_OVER);
    }

    private void entrarPausa() {
        if (estado != EstadoJogo.JOGANDO || fadeDirecao != 0) {
            return;
        }
        cima = baixo = esquerda = direita = correr = false;
        estado = EstadoJogo.PAUSADO;
    }

    private void retomarJogo() {
        if (estado != EstadoJogo.PAUSADO) {
            return;
        }
        cima = baixo = esquerda = direita = correr = false;
        estado = EstadoJogo.JOGANDO;
        requestFocusInWindow();
    }

    private void iniciarTransicao(EstadoJogo novoEstado) {
        if (fadeDirecao != 0 || estado == novoEstado) {
            return;
        }
        estadoAlvo = novoEstado;
        fadeDirecao = 1;
        fadeAlpha = 0f;
        fecharOpcoes();
    }

    private Direcao direcaoPorVetor(double dx, double dy) {
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direcao.DIREITA : Direcao.ESQUERDA;
        }
        return dy > 0 ? Direcao.BAIXO : Direcao.CIMA;
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        desenharFundo(g2);
        desenharCenaJogo(g2);

        if (estado == EstadoJogo.JOGANDO) {
            desenharHUD(g2);
        } else if (estado == EstadoJogo.PAUSADO) {
            desenharHUD(g2);
            desenharPausa(g2);
        } else if (estado == EstadoJogo.MENU) {
            desenharMenu(g2);
        } else if (estado == EstadoJogo.GAME_OVER) {
            desenharGameOver(g2);
        } else if (estado == EstadoJogo.LOJA) {
            desenharLoja(g2);
        }
        if (opcoesVisiveis && podeMostrarOpcoes()) {
            desenharOverlayOpcoes(g2);
        }

        desenharTransicao(g2);
    }

    private void desenharFundo(Graphics2D g2) {
        boolean desenhouImagem = false;
        if (imagemFundo != null) {
            g2.drawImage(imagemFundo, 0, 0, largura, altura, null);
            desenhouImagem = true;
        }
        if (desenhouImagem) {
            g2.setComposite(AlphaComposite.SrcOver.derive(0.28f));
        }
        desenharChaoTiles(g2);
        g2.setComposite(AlphaComposite.SrcOver);

        GradientPaint gp = new GradientPaint(0, 0, new Color(26, 28, 34), 0, altura, new Color(18, 20, 26));
        float overlayAlpha = desenhouImagem ? 0.35f : 0.6f;
        g2.setComposite(AlphaComposite.SrcOver.derive(overlayAlpha));
        g2.setPaint(gp);
        g2.fillRect(0, 0, largura, altura);
        g2.setComposite(AlphaComposite.SrcOver);

    }

    private void desenharChaoTiles(Graphics2D g2) {
        if (tilesChaoVariacoes == null || tilesChaoVar == null || tilesCols == 0 || tilesRows == 0) {
            if (texturaChao != null) {
                g2.setPaint(texturaChao);
                g2.fillRect(0, 0, largura, altura);
            } else {
                g2.setColor(new Color(22, 24, 30));
                g2.fillRect(0, 0, largura, altura);
            }
            return;
        }
        for (int c = 0; c < tilesCols; c++) {
            int x = c * TILE_SIZE;
            for (int r = 0; r < tilesRows; r++) {
                int y = r * TILE_SIZE;
                int idx = tilesChaoVar[c][r];
                if (idx < 0 || idx >= tilesChaoVariacoes.length) {
                    idx = 0;
                }
                BufferedImage tile = tilesChaoVariacoes[idx];
                g2.drawImage(tile, x, y, TILE_SIZE, TILE_SIZE, null);
            }
        }
    }

    private void desenharCenaJogo(Graphics2D g2) {
        desenharPerigos(g2);
        desenharObstaculos(g2);
        desenharCroissant(g2);
        desenharPowerUp(g2);
        desenharInimigos(g2);
        desenharParticulas(g2);
        desenharEfeitos(g2);
        desenharJogador(g2);
    }

    private void desenharObstaculos(Graphics2D g2) {
        if (tilesParede == null || tileWallCenter == null) {
            if (texturaParede != null) {
                g2.setPaint(texturaParede);
                for (Obstaculo o : obstaculos) {
                    g2.fill(o.getBounds());
                }
            } else {
                g2.setColor(new Color(90, 90, 100));
                for (Obstaculo o : obstaculos) {
                    g2.fill(o.getBounds());
                }
            }
            g2.setColor(new Color(40, 40, 50));
            for (Obstaculo o : obstaculos) {
                g2.draw(o.getBounds());
            }
            return;
        }
        for (int c = 0; c < tilesCols; c++) {
            int x = c * TILE_SIZE;
            for (int r = 0; r < tilesRows; r++) {
                if (!tilesParede[c][r]) {
                    continue;
                }
                int y = r * TILE_SIZE;
                BufferedImage tile = escolherTileParede(c, r);
                g2.drawImage(tile, x, y, TILE_SIZE, TILE_SIZE, null);
            }
        }
    }

    private BufferedImage escolherTileParede(int c, int r) {
        boolean n = isParedeTile(c, r - 1);
        boolean s = isParedeTile(c, r + 1);
        boolean w = isParedeTile(c - 1, r);
        boolean e = isParedeTile(c + 1, r);
        if (!n && !w) {
            return tileWallCornerTL;
        }
        if (!n && !e) {
            return tileWallCornerTR;
        }
        if (!s && !w) {
            return tileWallCornerBL;
        }
        if (!s && !e) {
            return tileWallCornerBR;
        }
        if (!n) {
            return tileWallEdgeTop;
        }
        if (!s) {
            return tileWallEdgeBottom;
        }
        if (!w) {
            return tileWallEdgeLeft;
        }
        if (!e) {
            return tileWallEdgeRight;
        }
        return tileWallCenter;
    }

    private void desenharPerigos(Graphics2D g2) {
        for (Perigo p : perigos) {
            int x = (int) Math.round(p.x);
            int y = (int) Math.round(p.y);
            int w = (int) Math.round(p.w);
            int h = (int) Math.round(p.h);
            if (p.tipo == PerigoTipo.BURACO) {
                if (imagemBuraco != null) {
                    desenharImagemAjustada(g2, imagemBuraco, x, y, w, h, 1f);
                    continue;
                }
                g2.setColor(new Color(10, 10, 15));
                g2.fillOval(x, y, w, h);
                g2.setColor(new Color(60, 60, 70));
                g2.drawOval(x, y, w, h);
            } else if (p.tipo == PerigoTipo.ESCORREGADIO) {
                if (imagemGelo != null) {
                    desenharImagemAjustada(g2, imagemGelo, x, y, w, h, 0.9f);
                    continue;
                }
                g2.setColor(new Color(150, 190, 230, 140));
                g2.fillRoundRect(x, y, w, h, 12, 12);
                g2.setColor(new Color(200, 220, 240, 160));
                for (int i = 0; i < w; i += 20) {
                    g2.drawLine(x + i, y, x + i - 20, y + h);
                }
            } else if (p.tipo == PerigoTipo.ZONA_FOME) {
                if (imagemFogo != null) {
                    desenharImagemAjustada(g2, imagemFogo, x, y, w, h, 0.92f);
                    continue;
                }
                g2.setColor(new Color(220, 120, 80, 120));
                g2.fillRoundRect(x, y, w, h, 12, 12);
                g2.setColor(new Color(240, 150, 100, 170));
                g2.drawRoundRect(x, y, w, h, 12, 12);
            } else if (p.tipo == PerigoTipo.ARMADILHA) {
                if (p.ativo) {
                    if (imagemArmadilha != null) {
                        desenharImagemAjustada(g2, imagemArmadilha, x, y, w, h, 1f);
                        continue;
                    }
                    if (imagemFogo != null) {
                        desenharImagemAjustada(g2, imagemFogo, x, y, w, h, 0.85f);
                        continue;
                    }
                    g2.setColor(new Color(200, 60, 60, 180));
                    g2.fillRect(x, y, w, h);
                    g2.setColor(new Color(40, 10, 10));
                    g2.drawRect(x, y, w, h);
                } else {
                    if (imagemArmadilha != null) {
                        desenharImagemAjustada(g2, imagemArmadilha, x, y, w, h, 0.45f);
                        continue;
                    }
                    g2.setColor(new Color(80, 80, 90, 120));
                    g2.fillRect(x, y, w, h);
                    g2.setColor(new Color(60, 60, 70, 160));
                    g2.drawRect(x, y, w, h);
                }
            }
        }
    }

    private void desenharCroissant(Graphics2D g2) {
        if (croissant == null) {
            return;
        }
        double pulsar = 1.0 + 0.10 * Math.sin(animCroissantTempo * 4.0);
        int base = (int) Math.round(croissant.tamanho);
        int cs = (int) Math.round(base * pulsar);
        int cx = (int) Math.round(croissant.x + base / 2.0 - cs / 2.0);
        int cy = (int) Math.round(croissant.y + base / 2.0 - cs / 2.0);

        if (imagemCroissant != null) {
            double ang = Math.sin(animCroissantTempo * 2.6) * 0.25;
            AffineTransform old = g2.getTransform();
            g2.translate(cx + cs / 2.0, cy + cs / 2.0);
            g2.rotate(ang);
            g2.drawImage(imagemCroissant, -cs / 2, -cs / 2, cs, cs, null);
            g2.setTransform(old);
        } else {
            g2.setColor(new Color(235, 190, 90));
            g2.fillOval(cx, cy, cs, cs);
            g2.setColor(new Color(170, 120, 50));
            g2.drawOval(cx, cy, cs, cs);
        }
    }

    private void desenharPowerUp(Graphics2D g2) {
        desenharPickup(g2, powerUpAtual);
        desenharPickup(g2, itemEspecialAtual);
    }

    private void desenharPickup(Graphics2D g2, PowerUp pickup) {
        if (pickup == null) {
            return;
        }
        double pulsar = 1.0 + 0.12 * Math.sin(animPowerUpTempo * 5.0);
        int base = (int) Math.round(pickup.tamanho);
        int cs = (int) Math.round(base * pulsar);
        int cx = (int) Math.round(pickup.x + base / 2.0 - cs / 2.0);
        int cy = (int) Math.round(pickup.y + base / 2.0 - cs / 2.0);
        boolean raro = pickup.tipo == PowerUpTipo.INVENCIVEL
                || pickup.tipo == PowerUpTipo.REGEN_FOME
                || pickup.tipo == PowerUpTipo.PISTOLA
                || pickup.tipo == PowerUpTipo.VIDA;
        if (raro) {
            double glow = cs * 0.9;
            double gx = cx + cs / 2.0;
            double gy = cy + cs / 2.0;
            float[] dist = new float[] { 0f, 1f };
            Color baseCor = getCorPowerUp(pickup.tipo);
            Color baseGlow = new Color(baseCor.getRed(), baseCor.getGreen(), baseCor.getBlue(), 170);
            Color[] colors = new Color[] { baseGlow, new Color(baseGlow.getRed(), baseGlow.getGreen(), baseGlow.getBlue(), 0) };
            Paint old = g2.getPaint();
            g2.setPaint(new RadialGradientPaint(new Point2D.Double(gx, gy), (float) glow, dist, colors));
            g2.fillOval((int) (gx - glow), (int) (gy - glow), (int) (glow * 2), (int) (glow * 2));
            g2.setPaint(old);
        }
        BufferedImage img;
        if (pickup.tipo == PowerUpTipo.VELOCIDADE) {
            img = imagemPowerVelocidade;
        } else if (pickup.tipo == PowerUpTipo.INVENCIVEL) {
            img = imagemPowerInvencivel;
        } else if (pickup.tipo == PowerUpTipo.PISTOLA) {
            img = imagemPowerPistola;
        } else if (pickup.tipo == PowerUpTipo.VIDA) {
            img = imagemPowerVida;
        } else {
            img = imagemPowerFome;
        }
        g2.drawImage(img, cx, cy, cs, cs, null);
    }

    private void desenharInimigos(Graphics2D g2) {
        for (Inimigo i : inimigos) {
            int x = (int) Math.round(i.x);
            int y = (int) Math.round(i.y);
            int s = (int) Math.round(i.tamanho);
            BufferedImage img = imagemInimigos.get(i.tipo);
            g2.drawImage(img, x, y, s, s, null);
        }
    }

    private void desenharParticulas(Graphics2D g2) {
        for (Particula p : particulas) {
            float alpha = (float) (p.vida / p.vidaTotal);
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2.setColor(p.cor);
            int size = (int) Math.max(2, 4 * alpha + 2);
            int x = (int) Math.round(p.x - size / 2.0);
            int y = (int) Math.round(p.y - size / 2.0);
            g2.fillOval(x, y, size, size);
        }
        g2.setComposite(AlphaComposite.SrcOver);
    }

    private void desenharEfeitos(Graphics2D g2) {
        long agora = System.currentTimeMillis();
        for (Efeito e : efeitos) {
            double t = (agora - e.inicioMs) / (double) e.duracaoMs;
            t = Math.min(1.0, Math.max(0.0, t));
            int raio = (int) (10 + 22 * t);
            int x = (int) Math.round(e.x - raio / 2.0);
            int y = (int) Math.round(e.y - raio / 2.0);
            g2.setComposite(AlphaComposite.SrcOver.derive((float) (1.0 - t)));
            g2.setColor(e.cor);
            g2.drawOval(x, y, raio, raio);
            g2.setComposite(AlphaComposite.SrcOver);
        }
    }

    private void desenharJogador(Graphics2D g2) {
        long agora = System.currentTimeMillis();
        boolean piscar = agora < danoAteMs && (agora / 120) % 2 == 0;
        if (piscar) {
            return;
        }

        int px = (int) Math.round(seuBarriga.x);
        int py = (int) Math.round(seuBarriga.y);
        int ps = (int) Math.round(seuBarriga.tamanho);
        int vs = (int) Math.round(ps * ESCALA_JOGADOR_VISUAL);
        int vx = (int) Math.round(px + ps / 2.0 - vs / 2.0);
        int vy = (int) Math.round(py + ps / 2.0 - vs / 2.0);

        BufferedImage frame = seuBarriga.getFrame();
        if (frame != null) {
            g2.drawImage(frame, vx, vy, vs, vs, null);
        } else if (imagemBarriga != null) {
            g2.drawImage(imagemBarriga, vx, vy, vs, vs, null);
        } else {
            g2.setColor(new Color(50, 100, 220));
            g2.fill(new Rectangle2D.Double(vx, vy, vs, vs));
        }

        if (powerUpAtivo != null && agora < powerUpAteMs) {
            Color aura = powerUpAtivo == PowerUpTipo.VELOCIDADE
                    ? new Color(80, 170, 255, 130)
                    : (powerUpAtivo == PowerUpTipo.INVENCIVEL ? new Color(255, 220, 100, 130) : new Color(120, 220, 120, 130));
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(aura);
            g2.drawOval(vx - 4, vy - 4, vs + 8, vs + 8);
            g2.setStroke(new BasicStroke(1f));
        }
    }

    private void desenharHUD(Graphics2D g2) {
        double escala = Math.min((double) largura / BASE_LARGURA, (double) altura / BASE_ALTURA);
        int margem = (int) (18 * escala);
        int alturaPainel = (int) (84 * escala);
        long agoraMsHud = System.currentTimeMillis();
        int painelX = margem;
        int painelY = altura - alturaPainel - margem;
        int painelW = largura - margem * 2;
        int pad = (int) (16 * escala);
        int heartsX = painelX + pad;
        int heartSize = (int) (22 * escala);
        int heartGap = (int) (6 * escala);
        int heartsY = painelY + (alturaPainel - heartSize) / 2;
        int shownHearts = Math.min(vidas, 10);
        for (int i = 0; i < shownHearts; i++) {
            g2.drawImage(imagemCoracao, heartsX + i * (heartSize + heartGap), heartsY, heartSize, heartSize, null);
        }
        if (vidas > 10) {
            g2.setFont(fonteHUDMini.deriveFont((float) (14 * escala)));
            desenharTextoSombra(g2, "+" + (vidas - 10), heartsX + shownHearts * (heartSize + heartGap), heartsY + heartSize / 2,
                    new Color(200, 200, 220));
        }

        g2.setFont(fonteHUD.deriveFont((float) (15 * escala)));
        String croissantTexto = "Croissants " + croissantsColetados;
        FontMetrics croissantFm = g2.getFontMetrics();
        int croissantWidth = croissantFm.stringWidth(croissantTexto);
        int croissantY = heartsY + heartSize + (int) (14 * escala);
        desenharTextoSombra(g2, croissantTexto, heartsX, croissantY, new Color(220, 220, 230));

        String missaoTexto = missaoAtual != null
                ? "Missao: " + missaoAtual.getTextoHUD(System.currentTimeMillis())
                : "Missao: aguardando...";
        g2.setFont(fonteHUDMini.deriveFont((float) (16 * escala)));
        FontMetrics missaoFm = g2.getFontMetrics();
        int missaoWidth = missaoFm.stringWidth(missaoTexto);

        String labelFome = "Fome";
        String labelStamina = "Stamina";
        g2.setFont(fonteHUDMini.deriveFont((float) (14 * escala)));
        FontMetrics labelFm = g2.getFontMetrics();
        int labelFomeWidth = labelFm.stringWidth(labelFome);
        int labelStaminaWidth = labelFm.stringWidth(labelStamina);
        int barW = (int) (90 * escala);
        int barH = Math.max((int) (10 * escala), 8);
        int gapSmall = (int) (12 * escala);
        int statsBlockWidth = labelFomeWidth + gapSmall + barW + gapSmall + labelStaminaWidth + gapSmall + barW;
        int statsX = painelX + painelW - pad - statsBlockWidth;
        int barY = painelY + (alturaPainel - barH) / 2;

        int fomeLabelX = statsX;
        int fomeBarX = fomeLabelX + labelFomeWidth + gapSmall;
        int staminaLabelX = fomeBarX + barW + gapSmall;
        int staminaBarX = staminaLabelX + labelStaminaWidth + gapSmall;

        desenharBarra(g2, fomeBarX, barY, barW, barH, fomeAtual / fomeMax,
                fomeAtual / fomeMax <= FOME_CRITICA_RATIO ? new Color(230, 80, 70) : new Color(230, 160, 70), "");
        desenharBarra(g2, staminaBarX, barY, barW, barH, staminaAtual / staminaMax,
                staminaExausta ? new Color(120, 120, 120) : new Color(90, 160, 230), "");
        g2.setFont(fonteHUDMini.deriveFont((float) (14 * escala)));
        desenharTextoSombra(g2, labelFome, fomeLabelX, barY + barH / 2 + labelFm.getAscent() / 2,
                new Color(200, 210, 230));
        desenharTextoSombra(g2, labelStamina, staminaLabelX, barY + barH / 2 + labelFm.getAscent() / 2,
                new Color(200, 210, 230));

        int heartsWidth = shownHearts * (heartSize + heartGap);
        int missionAreaStart = Math.max(heartsX + heartsWidth + pad, heartsX + croissantWidth + pad);
        int missionAreaEnd = statsX - pad;
        int missionX = missionAreaStart;
        if (missionAreaEnd - missionAreaStart > missaoWidth) {
            int centerTarget = painelX + painelW / 2 - missaoWidth / 2;
            missionX = Math.max(missionAreaStart, Math.min(centerTarget, missionAreaEnd - missaoWidth));
        }
        int missionY = barY + barH / 2 + missaoFm.getAscent() / 2 - 2;
        g2.setFont(fonteHUDMini.deriveFont((float) (16 * escala)));
        desenharTextoSombra(g2, missaoTexto, missionX, missionY, new Color(230, 230, 240));

        String ammoTexto = "Municao: " + balasPistola;
        String powerTexto;
        if (powerUpAtivo != null && agoraMsHud < powerUpAteMs) {
            double tempoRestante = Math.max(0, (powerUpAteMs - agoraMsHud) / 1000.0);
            tempoRestante = Math.round(tempoRestante * 10.0) / 10.0;
            powerTexto = getNomePowerUp(powerUpAtivo) + " " + String.format(Locale.US, "%.1f", tempoRestante) + "s";
        } else {
            powerTexto = "Power-up: nenhum";
        }
        g2.setFont(fonteHUDMini.deriveFont((float) (14 * escala)));
        FontMetrics infoFm = g2.getFontMetrics();
        int infoWidth = Math.max(infoFm.stringWidth(ammoTexto), infoFm.stringWidth(powerTexto));
        int infoX = Math.max(missionAreaStart, missionAreaEnd - infoWidth);
        int powerY = painelY + alturaPainel - pad;
        int ammoY = powerY - infoFm.getHeight();
        Color infoColor = new Color(220, 220, 235);
        desenharTextoSombra(g2, ammoTexto, infoX, ammoY, infoColor);
        desenharTextoSombra(g2, powerTexto, infoX, powerY, infoColor);
    }

    private void desenharBarra(Graphics2D g2, int x, int y, int w, int h, double ratio, Color cor, String label) {
        ratio = clamp(ratio, 0, 1);
        int arc = Math.max(6, h);
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(x, y, w, h, arc, arc);
        int fillW = (int) Math.round(w * ratio);
        if (fillW > 0) {
            g2.setColor(cor);
            g2.fillRoundRect(x, y, fillW, h, arc, arc);
        }
        g2.setColor(new Color(255, 255, 255, 40));
        g2.drawLine(x + 3, y + 2, x + w - 3, y + 2);
        g2.setColor(new Color(0, 0, 0, 180));
        g2.drawRoundRect(x, y, w, h, arc, arc);
        desenharTextoSombra(g2, label, x, y - 4, Color.WHITE);
    }

    private void desenharPainel(Graphics2D g2, int x, int y, int w, int h, Color base, Color borda, int arco) {
        Paint old = g2.getPaint();
        GradientPaint gp = new GradientPaint(x, y, base.brighter(), x, y + h, base.darker());
        g2.setPaint(gp);
        g2.fillRoundRect(x, y, w, h, arco, arco);
        g2.setPaint(old);
        g2.setColor(borda);
        g2.drawRoundRect(x, y, w, h, arco, arco);
        g2.setColor(new Color(255, 255, 255, 30));
        g2.drawLine(x + 10, y + 6, x + w - 10, y + 6);
    }

    private void desenharVolumeBarras(Graphics2D g2, int x, int y, int width, int height, int barras) {
        float volume = (float) audio.getVolume();
        int ativos = Math.round(volume * barras);
        ativos = Math.max(0, Math.min(barras, ativos));
        int spacing = Math.max(3, height / 2);
        int totalSpacing = spacing * (barras - 1);
        int barW = (width - totalSpacing) / barras;
        if (barW <= 0) {
            barW = Math.max(4, width / barras);
            spacing = Math.max(2, spacing / 2);
        }
        for (int i = 0; i < barras; i++) {
            int bx = x + i * (barW + spacing);
            int bw = barW;
            int bh = height;
            Color fill = i < ativos ? new Color(140, 210, 255) : new Color(80, 85, 100, 210);
            g2.setColor(fill);
            g2.fillRoundRect(bx, y, bw, bh, bh, bh);
            g2.setColor(new Color(0, 0, 0, 120));
            g2.drawRoundRect(bx, y, bw, bh, bh, bh);
        }
    }

    private void desenharOpcoesVolume(Graphics2D g2, int x, int y, int width, int height, double escala) {
        desenharPainel(g2, x, y, width, height,
                new Color(18, 20, 30, 220), new Color(90, 95, 110, 200), (int) (16 * escala));
        g2.setFont(fonteHUDMini.deriveFont((float) (14 * escala)));
        desenharTextoSombra(g2, "Opcoes", x + (int) (16 * escala), y + (int) (28 * escala), Color.WHITE);

        int barX = x + (int) (18 * escala);
        int barY = y + (int) (52 * escala);
        int barW = width - (int) (40 * escala);
        int barH = Math.max((int) (20 * escala), 18);
        int barras = Math.max(8, (int) (8 * escala));
        desenharVolumeBarras(g2, barX, barY, barW, barH, barras);

        g2.setFont(fonteHUDMini.deriveFont((float) (13 * escala)));
        int pct = (int) Math.round(audio.getVolume() * 100.0);
        desenharTextoSombra(g2, "Volume: " + pct + "%", barX, barY + barH + (int) (18 * escala),
                new Color(210, 210, 220));

        int btnH = Math.max((int) (38 * escala), (int) (34 * escala));
        int btnW = (int) (120 * escala);
        int btnY = y + height - btnH - (int) (12 * escala);
        int btnGap = (int) (12 * escala);
        int minusX = x + (int) (16 * escala);
        int plusX = minusX + btnW + btnGap;
        botaoVolumeMenos.setBounds(minusX, btnY, btnW, btnH);
        botaoVolumeMais.setBounds(plusX, btnY, btnW, btnH);
        Color base = new Color(140, 190, 240);
        desenharBotao(g2, botaoVolumeMenos, "Volume -", base, Color.BLACK,
                fonteHUDMini.deriveFont((float) (13 * escala)));
        desenharBotao(g2, botaoVolumeMais, "Volume +", base, Color.BLACK,
                fonteHUDMini.deriveFont((float) (13 * escala)));

        long agora = System.currentTimeMillis();
        if (agora < segredoVolumeMensagemAteMs) {
            g2.setFont(fonteHUDMini.deriveFont((float) (14 * escala)));
            desenharTextoSombra(g2, "Codigo secreto liberou +100 pts!", barX, btnY - (int) (10 * escala),
                    new Color(160, 255, 190));
        }
    }

    private void desenharFecharOpcoes(Graphics2D g2, int x, int y, int width, int height, double escala) {
        int size = Math.max((int) (30 * escala), (int) (28 * escala));
        int px = x + width - size - (int) (12 * escala);
        int py = y + (int) (8 * escala);
        botaoFecharOpcoes.setBounds(px, py, size, size);
        g2.setColor(new Color(255, 255, 255, 160));
        g2.fillOval(px, py, size, size);
        g2.setColor(new Color(30, 30, 30));
        g2.setStroke(new BasicStroke(Math.max(2f, size / 10f)));
        g2.drawLine(px + 6, py + 6, px + size - 6, py + size - 6);
        g2.drawLine(px + size - 6, py + 6, px + 6, py + size - 6);
        g2.setStroke(new BasicStroke(1f));
    }

    private boolean podeMostrarOpcoes() {
        return estado == EstadoJogo.MENU || estado == EstadoJogo.PAUSADO || estado == EstadoJogo.GAME_OVER;
    }

    private void desenharOverlayOpcoes(Graphics2D g2) {
        if (!opcoesVisiveis || !podeMostrarOpcoes()) {
            return;
        }
        double escala = Math.min((double) largura / BASE_LARGURA, (double) altura / BASE_ALTURA);
        int width = (int) (360 * escala);
        int height = (int) (220 * escala);
        int x = (largura - width) / 2;
        int y = (altura - height) / 2;
        Composite old = g2.getComposite();
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, largura, altura);
        g2.setComposite(old);
        desenharOpcoesVolume(g2, x, y, width, height, escala);
        desenharFecharOpcoes(g2, x, y, width, height, escala);
        painelOpcoesBounds.setBounds(x, y, width, height);
    }

    private void fecharOpcoes() {
        opcoesVisiveis = false;
        painelOpcoesBounds.setBounds(0, 0, 0, 0);
    }

    private void desenharImagemAjustada(Graphics2D g2, BufferedImage img, int x, int y, int w, int h, float alpha) {
        if (img == null) {
            return;
        }
        Composite old = g2.getComposite();
        float a = Math.max(0f, Math.min(1f, alpha));
        if (a < 1f) {
            g2.setComposite(AlphaComposite.SrcOver.derive(a));
        }
        g2.drawImage(img, x, y, w, h, null);
        g2.setComposite(old);
    }

    private void desenharTextoSombra(Graphics2D g2, String texto, int x, int y, Color cor) {
        int offset = Math.max(1, Math.round(g2.getFont().getSize2D() / 14f));
        g2.setColor(new Color(0, 0, 0, 150));
        g2.drawString(texto, x + offset, y + offset);
        g2.setColor(cor);
        g2.drawString(texto, x, y);
    }

    private void desenharBotao(Graphics2D g2, Rectangle r, String texto, Color base, Color textoCor, Font fonte) {
        Paint old = g2.getPaint();
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(r.x + 2, r.y + 3, r.width, r.height, 16, 16);
        GradientPaint gp = new GradientPaint(r.x, r.y, base.brighter(), r.x, r.y + r.height, base.darker());
        g2.setPaint(gp);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 16, 16);
        g2.setPaint(old);
        g2.setColor(new Color(255, 255, 255, 60));
        g2.drawLine(r.x + 8, r.y + 6, r.x + r.width - 8, r.y + 6);
        g2.setColor(new Color(0, 0, 0, 140));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 16, 16);
        g2.setFont(fonte);
        FontMetrics fm = g2.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(texto)) / 2;
        int ty = r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent();
        desenharTextoSombra(g2, texto, tx, ty, textoCor);
    }

    private void desenharMenu(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, largura, altura);

        double escala = Math.min((double) largura / BASE_LARGURA, (double) altura / BASE_ALTURA);
        int cardW = (int) (520 * escala);
        int cardH = (int) (460 * escala);
        int cardX = (largura - cardW) / 2;
        int cardY = (int) (altura * 0.18);
        desenharPainel(g2, cardX, cardY, cardW, cardH,
                new Color(18, 20, 28, 230), new Color(90, 95, 110, 200), (int) (18 * escala));

        int tituloY = cardY + (int) (70 * escala);
        Font tituloFont = fonteTitulo.deriveFont((float) (34 * escala));
        g2.setFont(tituloFont);
        String titulo = seuBarriga.nome + " e os Croissants";
        int tw = g2.getFontMetrics().stringWidth(titulo);
        desenharTextoSombra(g2, titulo, cardX + (cardW - tw) / 2, tituloY, Color.WHITE);

        g2.setFont(fonteSubtitulo.deriveFont((float) (16 * escala)));
        String subtitulo = "Sobreviva, evolua e mantenha a fome sob controle";
        int sw = g2.getFontMetrics().stringWidth(subtitulo);
        desenharTextoSombra(g2, subtitulo, cardX + (cardW - sw) / 2, tituloY + (int) (32 * escala),
                new Color(210, 210, 220));

        int bw = (int) (240 * escala);
        int bh = (int) (54 * escala);
        int gapButtons = (int) (16 * escala);
        int buttonBlockH = bh * 3 + gapButtons * 2;
        int bx = (largura - bw) / 2;
        int by = cardY + cardH - buttonBlockH - (int) (22 * escala);
        botaoIniciar = new Rectangle(bx, by, bw, bh);
        botaoOpcoesMenu = new Rectangle(bx, by + (bh + gapButtons), bw, bh);
        botaoSairMenu = new Rectangle(bx, by + (bh + gapButtons) * 2, bw, bh);

        desenharBotao(g2, botaoIniciar, "Iniciar", new Color(255, 205, 140), Color.BLACK,
                fonteUI.deriveFont((float) (18 * escala)));
        desenharBotao(g2, botaoOpcoesMenu, "Opcoes", new Color(140, 190, 240), Color.BLACK,
                fonteUI.deriveFont((float) (18 * escala)));
        desenharBotao(g2, botaoSairMenu, "Sair", new Color(220, 120, 120), Color.WHITE,
                fonteUI.deriveFont((float) (18 * escala)));
    }

    private void desenharGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 190));
        g2.fillRect(0, 0, largura, altura);

        double escala = Math.min((double) largura / BASE_LARGURA, (double) altura / BASE_ALTURA);
        int cardW = (int) (520 * escala);
        int cardH = (int) (380 * escala);
        int cardX = (largura - cardW) / 2;
        int cardY = (int) (altura * 0.24);
        desenharPainel(g2, cardX, cardY, cardW, cardH,
                new Color(18, 20, 28, 220), new Color(90, 95, 110, 180), (int) (18 * escala));

        int tituloY = cardY + (int) (70 * escala);
        g2.setFont(fonteTitulo.deriveFont((float) (32 * escala)));
        String titulo = "Game Over";
        int tw = g2.getFontMetrics().stringWidth(titulo);
        desenharTextoSombra(g2, titulo, cardX + (cardW - tw) / 2, tituloY, Color.WHITE);

        g2.setFont(fonteSubtitulo.deriveFont((float) (16 * escala)));
        String linha1 = "Pontuacao final: " + pontuacao;
        String linha2 = "Melhor pontuacao: " + melhorPontuacao;
        int l1w = g2.getFontMetrics().stringWidth(linha1);
        int l2w = g2.getFontMetrics().stringWidth(linha2);
        desenharTextoSombra(g2, linha1, cardX + (cardW - l1w) / 2,
                tituloY + (int) (38 * escala), new Color(220, 220, 230));
        desenharTextoSombra(g2, linha2, cardX + (cardW - l2w) / 2,
                tituloY + (int) (60 * escala), new Color(200, 210, 220));

        int bw = (int) (230 * escala);
        int bh = (int) (52 * escala);
        int gapButtons = (int) (18 * escala);
        int buttonBlockH = bh * 4 + gapButtons * 3;
        int bx = (largura - bw) / 2;
        int by = cardY + cardH - buttonBlockH - (int) (20 * escala);
        botaoIniciar = new Rectangle(bx, by, bw, bh);
        botaoLoja = new Rectangle(bx, by + (bh + gapButtons), bw, bh);
        botaoSairGameOver = new Rectangle(bx, by + (bh + gapButtons) * 2, bw, bh);
        botaoOpcoesGameOver = new Rectangle(bx, by + (bh + gapButtons) * 3, bw, bh);

        desenharBotao(g2, botaoIniciar, "Jogar de novo", new Color(255, 200, 120), Color.BLACK,
                fonteUI.deriveFont((float) (17 * escala)));
        desenharBotao(g2, botaoLoja, "Loja", new Color(140, 190, 240), Color.BLACK,
                fonteUI.deriveFont((float) (17 * escala)));
        desenharBotao(g2, botaoSairGameOver, "Sair", new Color(220, 120, 120), Color.WHITE,
                fonteUI.deriveFont((float) (17 * escala)));
        desenharBotao(g2, botaoOpcoesGameOver, "Opcoes", new Color(140, 190, 240), Color.BLACK,
                fonteUI.deriveFont((float) (17 * escala)));
    }

    private void desenharPausa(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, largura, altura);

        double escala = Math.min((double) largura / BASE_LARGURA, (double) altura / BASE_ALTURA);
        int cardW = (int) (460 * escala);
        int cardH = (int) (340 * escala);
        int cardX = (largura - cardW) / 2;
        int cardY = (int) (altura * 0.28);
        desenharPainel(g2, cardX, cardY, cardW, cardH,
                new Color(18, 20, 28, 220), new Color(90, 95, 110, 180), (int) (18 * escala));

        int tituloY = cardY + (int) (64 * escala);
        g2.setFont(fonteTitulo.deriveFont((float) (30 * escala)));
        String titulo = "Pausado";
        int tw = g2.getFontMetrics().stringWidth(titulo);
        desenharTextoSombra(g2, titulo, cardX + (cardW - tw) / 2, tituloY, Color.WHITE);

        int bw = (int) (230 * escala);
        int bh = (int) (52 * escala);
        int gap = (int) (16 * escala);
        int buttonBlockH = bh * 4 + gap * 3;
        int bx = (largura - bw) / 2;
        int by = cardY + cardH - buttonBlockH - (int) (18 * escala);
        botaoPausaContinuar.setBounds(bx, by, bw, bh);
        botaoPausaReiniciar.setBounds(bx, by + bh + gap, bw, bh);
        botaoPausaSair.setBounds(bx, by + (bh + gap) * 2, bw, bh);
        botaoOpcoesPausa.setBounds(bx, by + (bh + gap) * 3, bw, bh);

        desenharBotao(g2, botaoPausaContinuar, "Continuar", new Color(255, 200, 120), Color.BLACK,
                fonteUI.deriveFont((float) (17 * escala)));
        desenharBotao(g2, botaoPausaReiniciar, "Jogar de novo", new Color(140, 190, 240), Color.BLACK,
                fonteUI.deriveFont((float) (17 * escala)));
        desenharBotao(g2, botaoPausaSair, "Sair", new Color(220, 120, 120), Color.WHITE,
                fonteUI.deriveFont((float) (17 * escala)));
        desenharBotao(g2, botaoOpcoesPausa, "Opcoes", new Color(140, 190, 240), Color.BLACK,
                fonteUI.deriveFont((float) (17 * escala)));
    }

    private void desenharLoja(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, largura, altura);

        double escala = Math.min((double) largura / BASE_LARGURA, (double) altura / BASE_ALTURA);
        int tituloY = (int) (altura * 0.14);
        g2.setFont(fonteTitulo.deriveFont((float) (30 * escala)));
        String titulo = "Loja de Upgrades";
        int tw = g2.getFontMetrics().stringWidth(titulo);
        desenharTextoSombra(g2, titulo, (largura - tw) / 2, tituloY, Color.WHITE);

        g2.setFont(fonteSubtitulo.deriveFont((float) (16 * escala)));
        String saldo = "Saldo: " + pontosLoja + " | Total: " + pontosTotais;
        int sw = g2.getFontMetrics().stringWidth(saldo);
        desenharTextoSombra(g2, saldo, (largura - sw) / 2, tituloY + (int) (28 * escala), new Color(210, 210, 220));

        int boxW = (int) (620 * escala);
        int boxH = (int) (70 * escala);
        int gap = (int) (14 * escala);
        int boxX = (largura - boxW) / 2;
        int painelW = boxW + (int) (60 * escala);
        int painelH = (boxH + gap) * lojaItens.length + (int) (220 * escala);
        int painelX = (largura - painelW) / 2;
        int painelY = tituloY + (int) (28 * escala);
        desenharPainel(g2, painelX, painelY, painelW, painelH,
                new Color(18, 20, 28, 220), new Color(90, 95, 110, 180), (int) (18 * escala));

        int startY = painelY + (int) (42 * escala);
        for (int i = 0; i < lojaItens.length; i++) {
            LojaItem item = lojaItens[i];
            int y = startY + i * (boxH + gap);
            int nivel = getNivelUpgrade(item.tipo);
            int custo = getCustoUpgrade(item.tipo);
            String desc = getDescricaoUpgrade(item.tipo);
            boolean podeComprar = pontosLoja >= custo;
            Color bordaBox = podeComprar ? new Color(120, 200, 140, 180) : new Color(90, 100, 120, 150);
            desenharPainel(g2, boxX, y, boxW, boxH,
                    new Color(26, 28, 36, 230), bordaBox, (int) (14 * escala));

            g2.setFont(fonteUI.deriveFont((float) (15 * escala)));
            desenharTextoSombra(g2, (i + 1) + ". " + item.nome, boxX + (int) (14 * escala),
                    y + (int) (24 * escala), Color.WHITE);
            g2.setFont(fonteHUDMini.deriveFont((float) (13 * escala)));
            desenharTextoSombra(g2, desc, boxX + (int) (14 * escala), y + (int) (44 * escala),
                    new Color(200, 200, 210));
            desenharTextoSombra(g2, "Nivel " + nivel + " | Custo " + custo, boxX + (int) (14 * escala),
                    y + (int) (62 * escala), new Color(160, 160, 180));

            int btnW = (int) (120 * escala);
            int btnH = (int) (32 * escala);
            int btnX = boxX + boxW - btnW - (int) (16 * escala);
            int btnY = y + (boxH - btnH) / 2;
            botoesCompra[i].setBounds(btnX, btnY, btnW, btnH);
            Color corBotao = podeComprar ? new Color(120, 200, 140) : new Color(90, 90, 100);
            desenharBotao(g2, botoesCompra[i], "Comprar", corBotao, Color.BLACK,
                    fonteHUDMini.deriveFont((float) (13 * escala)));
        }

        int skinCardW = (int) (520 * escala);
        int skinCardH = (int) (150 * escala);
        int skinCardX = (largura - skinCardW) / 2;
        int skinCardY = painelY + painelH + (int) (32 * escala);
        desenharPainel(g2, skinCardX, skinCardY, skinCardW, skinCardH,
                new Color(28, 30, 40, 220), new Color(220, 190, 90, 180), (int) (18 * escala));

        int iconSize = (int) (96 * escala);
        int iconY = skinCardY + (skinCardH - iconSize) / 2;
        g2.drawImage(imagemSkinQuico, skinCardX + (int) (18 * escala), iconY, iconSize, iconSize, null);

        int textX = skinCardX + iconSize + (int) (36 * escala);
        int textBase = skinCardY + (int) (36 * escala);
        g2.setFont(fonteUI.deriveFont((float) (18 * escala)));
        desenharTextoSombra(g2, "Skin lendaria do Quico", textX, textBase, Color.WHITE);
        g2.setFont(fonteHUDMini.deriveFont((float) (13 * escala)));
        desenharTextoSombra(g2, "- 3x forca e power-ups duram mais", textX, textBase + (int) (28 * escala),
                new Color(210, 210, 220));
        desenharTextoSombra(g2, "- 5 balas por arma, 10 vidas", textX, textBase + (int) (48 * escala),
                new Color(210, 210, 220));
        String status = temSkinLegendaria() ? "Skin ativada" : "Custo: " + CUSTO_SKIN_LEGENDARIA + " pts";
        desenharTextoSombra(g2, status, textX, textBase + (int) (68 * escala), new Color(170, 220, 190));

        int skinBtnW = (int) (140 * escala);
        int skinBtnH = (int) (38 * escala);
        int skinBtnX = skinCardX + skinCardW - skinBtnW - (int) (20 * escala);
        int skinBtnY = skinCardY + skinCardH - skinBtnH - (int) (16 * escala);
        botaoSkinLegendaria.setBounds(skinBtnX, skinBtnY, skinBtnW, skinBtnH);
        boolean podeSkin = !skinQuicoComprada && pontosLoja >= CUSTO_SKIN_LEGENDARIA;
        Color skinCor = skinQuicoComprada ? new Color(120, 120, 140) : (podeSkin ? new Color(255, 200, 120) : new Color(90, 90, 100));
        String skinTexto = skinQuicoComprada ? "Comprada" : "Comprar";
        desenharBotao(g2, botaoSkinLegendaria, skinTexto, skinCor, Color.BLACK,
                fonteHUDMini.deriveFont((float) (13 * escala)));

        int buttonY = skinCardY + skinCardH + (int) (30 * escala);
        int buttonW = (int) (180 * escala);
        int buttonH = (int) (46 * escala);
        int buttonGap = (int) (16 * escala);
        int buttonStartX = (largura - (buttonW * 2 + buttonGap)) / 2;
        botaoLojaVoltar.setBounds(buttonStartX, buttonY, buttonW, buttonH);
        botaoLojaJogar.setBounds(buttonStartX + buttonW + buttonGap, buttonY, buttonW, buttonH);

        desenharBotao(g2, botaoLojaVoltar, "Voltar", new Color(255, 205, 140), Color.BLACK,
                fonteUI.deriveFont((float) (15 * escala)));
        desenharBotao(g2, botaoLojaJogar, "Jogar", new Color(140, 190, 240), Color.BLACK,
                fonteUI.deriveFont((float) (15 * escala)));

        int buttonY2 = buttonY + buttonH + buttonGap;
        botaoSairLoja.setBounds((largura - buttonW) / 2, buttonY2, buttonW, buttonH);
        desenharBotao(g2, botaoSairLoja, "Sair", new Color(220, 120, 120), Color.WHITE,
                fonteUI.deriveFont((float) (15 * escala)));
    }

    private void processarCliquePausa(Point p) {
        if (botaoPausaContinuar.contains(p)) {
            retomarJogo();
        } else if (botaoPausaReiniciar.contains(p)) {
            iniciarTransicao(EstadoJogo.JOGANDO);
        } else if (botaoPausaSair.contains(p)) {
            System.exit(0);
        }
    }

    private void processarCliqueLoja(Point p) {
        for (int i = 0; i < botoesCompra.length; i++) {
            if (botoesCompra[i] != null && botoesCompra[i].contains(p)) {
                comprarUpgradePorIndice(i);
                return;
            }
        }
        if (botaoSkinLegendaria.contains(p)) {
            comprarSkinLegendaria();
            return;
        }
        if (botaoLojaVoltar.contains(p)) {
            iniciarTransicao(EstadoJogo.GAME_OVER);
        } else if (botaoLojaJogar.contains(p)) {
            iniciarTransicao(EstadoJogo.JOGANDO);
        } else if (botaoSairLoja.contains(p)) {
            System.exit(0);
        }
    }

    private void processarCliqueVolumeMenos() {
        double volumeAtual = audio.getVolume();
        if (volumeAtual <= 0.001) {
            contadorSegredoVolume++;
            if (contadorSegredoVolume >= 3) {
                contadorSegredoVolume = 0;
                pontosLoja += 100;
                pontosTotais += 100;
                segredoVolumeMensagemAteMs = System.currentTimeMillis() + 2200;
            }
        } else {
            contadorSegredoVolume = 0;
        }
        audio.ajustarVolume(-0.1);
    }

    private void desenharTransicao(Graphics2D g2) {
        if (fadeAlpha <= 0f) {
            return;
        }
        g2.setComposite(AlphaComposite.SrcOver.derive(fadeAlpha));
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, largura, altura);
        g2.setComposite(AlphaComposite.SrcOver);
    }

    private static class LojaItem {
        final UpgradeTipo tipo;
        final String nome;
        final int custoBase;

        LojaItem(UpgradeTipo tipo, String nome, int custoBase) {
            this.tipo = tipo;
            this.nome = nome;
            this.custoBase = custoBase;
        }
    }

    private static class AudioManager {
        private final Clip sfxCroissant;
        private final Clip sfxDano;
        private final Clip sfxPowerUp;
        private final Clip sfxGameOver;
        private final Clip sfxPistola;
        private final Mp3Player sfxCroissantMp3;
        private final Mp3Player sfxDanoMp3;
        private final Mp3Player sfxPowerUpMp3;
        private final Mp3Player sfxGameOverMp3;
        private final Mp3Player sfxPistolaMp3;
        private final Clip musicaBase;
        private final Clip musicaRapida;
        private final Mp3Player musicaBaseMp3;
        private final Mp3Player musicaRapidaMp3;
        private Clip musicaAtual;
        private Mp3Player musicaAtualMp3;
        private int musicaTier = -1;
        private float volume = 0.8f;

        AudioManager() {
            sfxCroissantMp3 = carregarMp3("assets/hd/croissant_eat.mp3", "assets/hd/croissant.mp3",
                    "croissant_eat.mp3", "assets/croissant_eat.mp3",
                    "croissant.mp3", "assets/croissant.mp3",
                    "croassaint_eat.mp3", "assets/croassaint_eat.mp3",
                    "croassaint.mp3", "assets/croassaint.mp3");
            sfxDanoMp3 = carregarMp3("assets/hd/dano.mp3", "dano.mp3", "assets/dano.mp3");
            sfxPowerUpMp3 = carregarMp3("assets/hd/powerup.mp3", "powerup.mp3", "assets/powerup.mp3");
            sfxGameOverMp3 = carregarMp3("assets/hd/gameover.mp3", "gameover.mp3", "assets/gameover.mp3");
            sfxPistolaMp3 = carregarMp3("assets/hd/pistola.mp3", "pistola.mp3", "assets/pistola.mp3",
                    "assets/hd/gun.mp3", "gun.mp3", "assets/gun.mp3");
            musicaBaseMp3 = carregarMp3("assets/hd/musica.mp3", "musica.mp3", "assets/musica.mp3");
            musicaRapidaMp3 = carregarMp3("assets/hd/musica_fast.mp3", "musica_fast.mp3", "assets/musica_fast.mp3",
                    "assets/hd/musica_rapida.mp3", "musica_rapida.mp3", "assets/musica_rapida.mp3");
            sfxCroissant = carregarOuGerarClipMordida("croissant_eat.wav", "assets/croissant_eat.wav",
                    "croissant.wav", "assets/croissant.wav",
                    "assets/hd/croissant_eat.mp3", "croissant_eat.mp3", "assets/croissant_eat.mp3",
                    "assets/hd/croissant.mp3", "croissant.mp3", "assets/croissant.mp3");
            sfxDano = carregarOuGerarClip(240, 140, 0.45, "dano.wav", "assets/dano.wav");
            sfxPowerUp = carregarOuGerarClip(640, 120, 0.38, "powerup.wav", "assets/powerup.wav");
            sfxGameOver = carregarOuGerarClip(180, 220, 0.5, "gameover.wav", "assets/gameover.wav");
            sfxPistola = carregarOuGerarClipDisparo("pistola.wav", "assets/pistola.wav",
                    "gun.wav", "assets/gun.wav");
            musicaBase = carregarClip("musica.wav", "assets/musica.wav");
            musicaRapida = carregarClip("musica_fast.wav", "assets/musica_fast.wav");
            aplicarVolumeEmTodos();
        }

        void tocarCroissant() {
            tocarSfx(sfxCroissant, sfxCroissantMp3);
        }

        void tocarDano() {
            tocarSfx(sfxDano, sfxDanoMp3);
        }

        void tocarPowerUp() {
            tocarSfx(sfxPowerUp, sfxPowerUpMp3);
        }

        void tocarGameOver() {
            tocarSfx(sfxGameOver, sfxGameOverMp3);
        }

        void tocarPistola() {
            tocarSfx(sfxPistola, sfxPistolaMp3);
        }

        double getVolume() {
            return volume;
        }

        void ajustarVolume(double delta) {
            setVolume(volume + (float) delta);
        }

        void atualizarMusica(int nivel) {
            int tier = nivel >= 4 ? 1 : 0;
            if (tier == musicaTier) {
                return;
            }
            musicaTier = tier;
            Clip alvo = tier == 0 ? musicaBase : musicaRapida;
            Mp3Player alvoMp3 = tier == 0 ? musicaBaseMp3 : musicaRapidaMp3;
            tocarMusica(alvo, alvoMp3);
        }

        private void tocarMusica(Clip clip, Mp3Player mp3) {
            if (mp3 != null) {
                if (musicaAtualMp3 != null && musicaAtualMp3 != mp3) {
                    musicaAtualMp3.stop();
                }
                musicaAtualMp3 = mp3;
                mp3.setVolume(volume);
                mp3.play(true);
                if (musicaAtual != null) {
                    musicaAtual.stop();
                    musicaAtual.setFramePosition(0);
                    musicaAtual = null;
                }
                return;
            }
            if (clip == null) {
                pararMusica();
                return;
            }
            if (musicaAtual != null && musicaAtual != clip) {
                musicaAtual.stop();
                musicaAtual.setFramePosition(0);
            }
            musicaAtualMp3 = null;
            musicaAtual = clip;
            aplicarVolume(musicaAtual);
            if (musicaAtual.isRunning()) {
                return;
            }
            musicaAtual.setFramePosition(0);
            musicaAtual.loop(Clip.LOOP_CONTINUOUSLY);
        }

        private void pararMusica() {
            if (musicaAtual != null) {
                musicaAtual.stop();
                musicaAtual.setFramePosition(0);
            }
            if (musicaAtualMp3 != null) {
                musicaAtualMp3.stop();
            }
        }

        private void tocarSfx(Clip clip, Mp3Player mp3) {
            if (mp3 != null) {
                mp3.setVolume(volume);
                mp3.play(false);
                return;
            }
            if (clip == null) {
                return;
            }
            aplicarVolume(clip);
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        }

        private void setVolume(float value) {
            volume = Math.max(0.0f, Math.min(1.0f, value));
            aplicarVolumeEmTodos();
        }

        private void aplicarVolumeEmTodos() {
            aplicarVolume(sfxCroissant);
            aplicarVolume(sfxDano);
            aplicarVolume(sfxPowerUp);
            aplicarVolume(sfxGameOver);
            aplicarVolume(sfxPistola);
            aplicarVolume(musicaBase);
            aplicarVolume(musicaRapida);
            aplicarVolume(sfxCroissantMp3);
            aplicarVolume(sfxDanoMp3);
            aplicarVolume(sfxPowerUpMp3);
            aplicarVolume(sfxGameOverMp3);
            aplicarVolume(sfxPistolaMp3);
            aplicarVolume(musicaBaseMp3);
            aplicarVolume(musicaRapidaMp3);
        }

        private void aplicarVolume(Clip clip) {
            if (clip == null) {
                return;
            }
            if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                return;
            }
            FloatControl ctrl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float v = Math.max(0.0001f, volume);
            float db = (float) (20.0 * Math.log10(v));
            db = Math.max(ctrl.getMinimum(), Math.min(ctrl.getMaximum(), db));
            ctrl.setValue(db);
        }

        private void aplicarVolume(Mp3Player mp3) {
            if (mp3 == null) {
                return;
            }
            mp3.setVolume(volume);
        }

        private static Clip carregarOuGerarClip(double freq, int durMs, double volume, String... caminhos) {
            Clip clip = carregarClip(caminhos);
            if (clip != null) {
                return clip;
            }
            return gerarClipTone(freq, durMs, volume);
        }

        private static Clip carregarOuGerarClipMordida(String... caminhos) {
            Clip clip = carregarClip(caminhos);
            if (clip != null) {
                return clip;
            }
            return gerarClipMordida();
        }

        private static Clip carregarOuGerarClipDisparo(String... caminhos) {
            Clip clip = carregarClip(caminhos);
            if (clip != null) {
                return clip;
            }
            return gerarClipDisparo();
        }

        private static Clip gerarClipMordida() {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            int durMs = 160;
            int samples = (int) ((durMs / 1000.0) * format.getSampleRate());
            if (samples <= 0) {
                return null;
            }
            byte[] data = new byte[samples * 2];
            for (int i = 0; i < samples; i++) {
                double t = i / format.getSampleRate();
                double env = 1.0 - (i / (double) samples);
                double bite = t < 0.08 ? 1.0 : 0.65;
                double low = Math.sin(2.0 * Math.PI * 160.0 * t);
                double mid = Math.sin(2.0 * Math.PI * 520.0 * t) * 0.5;
                double val = (low * 0.6 + mid * 0.4) * env * bite;
                short sample = (short) (val * 20000);
                data[i * 2] = (byte) (sample & 0xff);
                data[i * 2 + 1] = (byte) ((sample >> 8) & 0xff);
            }
            try {
                Clip clip = AudioSystem.getClip();
                clip.open(format, data, 0, data.length);
                return clip;
            } catch (LineUnavailableException e) {
                return null;
            }
        }

        private static Clip gerarClipDisparo() {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            int durMs = 90;
            int samples = (int) ((durMs / 1000.0) * format.getSampleRate());
            if (samples <= 0) {
                return null;
            }
            byte[] data = new byte[samples * 2];
            Random r = new Random(7);
            for (int i = 0; i < samples; i++) {
                double t = i / format.getSampleRate();
                double env = 1.0 - (i / (double) samples);
                double noise = (r.nextDouble() * 2.0 - 1.0) * 0.6;
                double tone = Math.sin(2.0 * Math.PI * 900.0 * t) * 0.4;
                double val = (noise + tone) * env;
                short sample = (short) (val * 20000);
                data[i * 2] = (byte) (sample & 0xff);
                data[i * 2 + 1] = (byte) ((sample >> 8) & 0xff);
            }
            try {
                Clip clip = AudioSystem.getClip();
                clip.open(format, data, 0, data.length);
                return clip;
            } catch (LineUnavailableException e) {
                return null;
            }
        }

        private static Clip gerarClipTone(double freq, int durMs, double volume) {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            int samples = (int) ((durMs / 1000.0) * format.getSampleRate());
            if (samples <= 0) {
                return null;
            }
            byte[] data = new byte[samples * 2];
            int attack = Math.max(1, (int) (samples * 0.08));
            int release = Math.max(1, (int) (samples * 0.2));
            for (int i = 0; i < samples; i++) {
                double env = 1.0;
                if (i < attack) {
                    env *= i / (double) attack;
                } else if (i > samples - release) {
                    env *= (samples - i) / (double) release;
                }
                double angle = 2.0 * Math.PI * i * freq / format.getSampleRate();
                short value = (short) (Math.sin(angle) * 32767 * volume * env);
                data[i * 2] = (byte) (value & 0xff);
                data[i * 2 + 1] = (byte) ((value >> 8) & 0xff);
            }
            try {
                Clip clip = AudioSystem.getClip();
                clip.open(format, data, 0, data.length);
                return clip;
            } catch (LineUnavailableException e) {
                return null;
            }
        }

        private static Clip carregarClip(String... caminhos) {
            for (String caminho : caminhos) {
                if (caminho == null) {
                    continue;
                }
                File f = new File(caminho);
                if (!f.exists()) {
                    continue;
                }
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(f)) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    return clip;
                } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                    continue;
                }
            }
            return null;
        }

        private static Mp3Player carregarMp3(String... caminhos) {
            for (String caminho : caminhos) {
                if (caminho == null) {
                    continue;
                }
                File f = new File(caminho);
                if (!f.exists()) {
                    continue;
                }
                Mp3Player player = Mp3Player.criar(f);
                if (player != null) {
                    return player;
                }
            }
            return null;
        }

        private static class Mp3Player {
            private static boolean fxTentado;
            private static boolean fxDisponivel;
            private static Class<?> mediaClass;
            private static Class<?> playerClass;
            private static Class<?> durationClass;
            private static Object durationZero;
            private static int cycleIndefinite;
            private static java.lang.reflect.Method playMethod;
            private static java.lang.reflect.Method stopMethod;
            private static java.lang.reflect.Method seekMethod;
            private static java.lang.reflect.Method setCycleMethod;
            private static java.lang.reflect.Method setVolumeMethod;

            private final Object player;

            private Mp3Player(Object player) {
                this.player = player;
            }

            static Mp3Player criar(File file) {
                if (!garantirFx()) {
                    return null;
                }
                try {
                    String uri = file.toURI().toString();
                    Object media = mediaClass.getConstructor(String.class).newInstance(uri);
                    Object player = playerClass.getConstructor(mediaClass).newInstance(media);
                    return new Mp3Player(player);
                } catch (Exception e) {
                    return null;
                }
            }

            void play(boolean loop) {
                if (player == null) {
                    return;
                }
                try {
                    stopMethod.invoke(player);
                    seekMethod.invoke(player, durationZero);
                    setCycleMethod.invoke(player, loop ? cycleIndefinite : 1);
                    playMethod.invoke(player);
                } catch (Exception e) {
                }
            }

            void stop() {
                if (player == null) {
                    return;
                }
                try {
                    stopMethod.invoke(player);
                    seekMethod.invoke(player, durationZero);
                } catch (Exception e) {
                }
            }

            void setVolume(float volume) {
                if (player == null) {
                    return;
                }
                try {
                    double v = Math.max(0.0, Math.min(1.0, volume));
                    setVolumeMethod.invoke(player, v);
                } catch (Exception e) {
                }
            }

            private static boolean garantirFx() {
                if (fxTentado) {
                    return fxDisponivel;
                }
                fxTentado = true;
                try {
                    Class<?> panelClass = Class.forName("javafx.embed.swing.JFXPanel");
                    panelClass.getConstructor().newInstance();
                    mediaClass = Class.forName("javafx.scene.media.Media");
                    playerClass = Class.forName("javafx.scene.media.MediaPlayer");
                    durationClass = Class.forName("javafx.util.Duration");
                    durationZero = durationClass.getField("ZERO").get(null);
                    cycleIndefinite = playerClass.getField("INDEFINITE").getInt(null);
                    playMethod = playerClass.getMethod("play");
                    stopMethod = playerClass.getMethod("stop");
                    seekMethod = playerClass.getMethod("seek", durationClass);
                    setCycleMethod = playerClass.getMethod("setCycleCount", int.class);
                    setVolumeMethod = playerClass.getMethod("setVolume", double.class);
                    fxDisponivel = true;
                } catch (Exception e) {
                    fxDisponivel = false;
                }
                return fxDisponivel;
            }
        }
    }

    private static class Jogador {
        final String nome;
        double x;
        double y;
        final double tamanho;
        Direcao direcao = Direcao.BAIXO;
        boolean correndo;
        double deslizeX;
        double deslizeY;
        Animacao animacaoAtual = Animacao.PARADO;
        private Animacao animacaoAnterior = Animacao.PARADO;

        private BufferedImage[][] framesParado;
        private BufferedImage[][] framesAndar;
        private BufferedImage[][] framesCorrer;
        private BufferedImage[][] framesDano;
        private int frameIndex = 0;
        private double frameTempo = 0.0;

        Jogador(String nome, double x, double y, double tamanho) {
            this.nome = nome;
            this.x = x;
            this.y = y;
            this.tamanho = tamanho;
        }

        void definirFrames(BufferedImage[][] parado, BufferedImage[][] andar, BufferedImage[][] correr, BufferedImage[][] dano) {
            this.framesParado = parado;
            this.framesAndar = andar;
            this.framesCorrer = correr;
            this.framesDano = dano;
        }

        void atualizarAnimacao(double dt) {
            if (animacaoAtual != animacaoAnterior) {
                frameIndex = 0;
                frameTempo = 0.0;
                animacaoAnterior = animacaoAtual;
            }
            double intervalo;
            if (animacaoAtual == Animacao.CORRER) {
                intervalo = 0.09;
            } else if (animacaoAtual == Animacao.ANDAR) {
                intervalo = 0.14;
            } else if (animacaoAtual == Animacao.DANO) {
                intervalo = 0.08;
            } else {
                intervalo = 0.28;
            }
            frameTempo += dt;
            if (frameTempo >= intervalo) {
                frameTempo -= intervalo;
                frameIndex = (frameIndex + 1) % 2;
            }
        }

        void resetAnimacao() {
            frameIndex = 0;
            frameTempo = 0.0;
            animacaoAtual = Animacao.PARADO;
            animacaoAnterior = Animacao.PARADO;
        }

        BufferedImage getFrame() {
            int idx = indiceDirecao(direcao);
            BufferedImage[][] frames;
            if (animacaoAtual == Animacao.DANO) {
                frames = framesDano;
            } else if (animacaoAtual == Animacao.CORRER) {
                frames = framesCorrer;
            } else if (animacaoAtual == Animacao.ANDAR) {
                frames = framesAndar;
            } else {
                frames = framesParado;
            }
            if (frames == null) {
                return null;
            }
            return frames[idx][frameIndex];
        }

        Rectangle2D getBounds(double nx, double ny) {
            double hb = tamanho * HITBOX_JOGADOR_ESCALA;
            double offset = (tamanho - hb) / 2.0;
            return new Rectangle2D.Double(nx + offset, ny + offset, hb, hb);
        }

        Rectangle2D getBounds() {
            double hb = tamanho * HITBOX_JOGADOR_ESCALA;
            double offset = (tamanho - hb) / 2.0;
            return new Rectangle2D.Double(x + offset, y + offset, hb, hb);
        }

        private int indiceDirecao(Direcao d) {
            switch (d) {
                case CIMA:
                    return 0;
                case BAIXO:
                    return 1;
                case ESQUERDA:
                    return INVERTER_LADO_SPRITE ? 3 : 2;
                case DIREITA:
                    return INVERTER_LADO_SPRITE ? 2 : 3;
                default:
                    return 1;
            }
        }
    }

    private static class Croissant {
        final double x;
        final double y;
        final double tamanho;

        Croissant(double x, double y, double tamanho) {
            this.x = x;
            this.y = y;
            this.tamanho = tamanho;
        }

        Rectangle2D getBounds() {
            return new Rectangle2D.Double(x, y, tamanho, tamanho);
        }
    }

    private static class PowerUp {
        final double x;
        final double y;
        final double tamanho;
        final PowerUpTipo tipo;

        PowerUp(double x, double y, double tamanho, PowerUpTipo tipo) {
            this.x = x;
            this.y = y;
            this.tamanho = tamanho;
            this.tipo = tipo;
        }

        Rectangle2D getBounds() {
            return new Rectangle2D.Double(x, y, tamanho, tamanho);
        }
    }

    private static class Inimigo {
        double x;
        double y;
        final double tamanho;
        final InimigoTipo tipo;
        final double velocidadeBase;
        double dirX;
        double dirY;
        double ancoraX;
        double ancoraY;
        long perseguirAteMs;
        long proximaDecisaoMs;
        long proximoCacarMs;
        long lentoAteMs;

        Inimigo(double x, double y, double tamanho, InimigoTipo tipo, double velocidadeBase) {
            this.x = x;
            this.y = y;
            this.tamanho = tamanho;
            this.tipo = tipo;
            this.velocidadeBase = velocidadeBase;
            this.ancoraX = x;
            this.ancoraY = y;
        }

        Rectangle2D getBounds(double nx, double ny) {
            return new Rectangle2D.Double(nx, ny, tamanho, tamanho);
        }

        Rectangle2D getBounds() {
            return new Rectangle2D.Double(x, y, tamanho, tamanho);
        }
    }

    private static class Obstaculo {
        final double x;
        final double y;
        final double w;
        final double h;

        Obstaculo(double x, double y, double w, double h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        Rectangle2D getBounds() {
            return new Rectangle2D.Double(x, y, w, h);
        }
    }

    private static class Perigo {
        final double x;
        final double y;
        final double w;
        final double h;
        final PerigoTipo tipo;
        boolean ativo = true;
        long proximaTrocaMs = 0L;
        long tempoAtivoMs = 0L;
        long tempoInativoMs = 0L;

        Perigo(double x, double y, double w, double h, PerigoTipo tipo) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.tipo = tipo;
        }

        void configurarCiclo(long inicioMs, long tempoAtivoMs, long tempoInativoMs) {
            this.ativo = false;
            this.tempoAtivoMs = tempoAtivoMs;
            this.tempoInativoMs = tempoInativoMs;
            this.proximaTrocaMs = inicioMs + tempoInativoMs;
        }

        void atualizar(long agoraMs) {
            if (tipo != PerigoTipo.ARMADILHA) {
                return;
            }
            if (agoraMs >= proximaTrocaMs) {
                ativo = !ativo;
                proximaTrocaMs = agoraMs + (ativo ? tempoAtivoMs : tempoInativoMs);
            }
        }

        Rectangle2D getBounds() {
            return new Rectangle2D.Double(x, y, w, h);
        }
    }

    private static class Missao {
        final MissaoTipo tipo;
        int alvo = 0;
        int progresso = 0;
        long inicioMs = 0L;
        long duracaoMs = 0L;
        int bonusPontos = 0;
        boolean concluida = false;
        boolean falhou = false;

        Missao(MissaoTipo tipo) {
            this.tipo = tipo;
        }

        String getTextoHUD(long agoraMs) {
            if (tipo == MissaoTipo.CROISSANTS_SEM_DANO) {
                return "Colete " + progresso + "/" + alvo + " sem dano";
            }
            if (tipo == MissaoTipo.CROISSANTS_TEMPO) {
                long restante = Math.max(0L, duracaoMs - (agoraMs - inicioMs));
                return "Colete " + progresso + "/" + alvo + " em " + (int) Math.ceil(restante / 1000.0) + "s";
            }
            if (tipo == MissaoTipo.CROISSANTS_CORRER) {
                long restante = Math.max(0L, duracaoMs - (agoraMs - inicioMs));
                return "Colete " + progresso + "/" + alvo + " correndo (" + (int) Math.ceil(restante / 1000.0) + "s)";
            }
            long restante = Math.max(0L, duracaoMs - (agoraMs - inicioMs));
            return "Pegue " + progresso + "/" + alvo + " power-ups (" + (int) Math.ceil(restante / 1000.0) + "s)";
        }
    }

    private static class Particula {
        double x;
        double y;
        double vx;
        double vy;
        double vida;
        double vidaTotal;
        final Color cor;

        Particula(double x, double y, double vx, double vy, double vida, Color cor) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.vida = vida;
            this.vidaTotal = vida;
            this.cor = cor;
        }

        void atualizar(double dt) {
            x += vx * dt;
            y += vy * dt;
            vida -= dt;
            vx *= 0.98;
            vy *= 0.98;
        }
    }

    private static class Efeito {
        final double x;
        final double y;
        final long duracaoMs;
        final long inicioMs;
        final Color cor;

        Efeito(double x, double y, long duracaoMs, Color cor) {
            this.x = x;
            this.y = y;
            this.duracaoMs = duracaoMs;
            this.cor = cor;
            this.inicioMs = System.currentTimeMillis();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Seu Barriga e os Croissants");
            Jogo jogo = new Jogo();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(jogo);

            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (USAR_FULLSCREEN && gd.isFullScreenSupported()) {
                frame.setUndecorated(true);
                frame.setResizable(false);
                gd.setFullScreenWindow(frame);
            } else {
                frame.setResizable(false);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }

            if (!frame.isVisible()) {
                frame.setVisible(true);
            }
            jogo.requestFocusInWindow();
        });
    }
}












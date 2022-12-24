package cbot.components;

import arc.files.ZipFi;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureAtlas.TextureAtlasData;
import arc.graphics.g2d.TextureAtlas.TextureAtlasData.AtlasPage;
import arc.struct.ObjectMap;
import arc.util.*;
import cbot.Vars;
import mindustry.core.*;
import mindustry.world.Tile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import static arc.Core.*;
import static arc.graphics.g2d.Draw.scl;
import static arc.graphics.g2d.Lines.useLegacyLine;
import static arc.util.Log.info;
import static arc.util.serialization.Jval.read;
import static mindustry.Vars.*;
import static mindustry.content.Items.*;

public class ResourceUtils {

    public static void init() {
        downloadResources();

        content = new ContentLoader();
        state = new GameState();

        content.createBaseContent();

        loadIgnoreErrors(content::init);
        loadTextureDatas();
        loadIgnoreErrors(content::load);

        loadBlockColors();
        loadItemEmojis();

        world = new World() {
            public Tile tile(int x, int y) {
                return new Tile(x, y);
            }
        };

        useLegacyLine = true;
        scl = 1f / 4f;
    }

    private static void downloadResources() {
        var mindustry = Vars.resources.child("Mindustry.jar");
        if (mindustry.exists()) return;

        Http.get("https://api.github.com/repos/Anuken/Mindustry/releases/81624846").timeout(0).block(release -> {
            var assets = read(release.getResultAsString()).get("assets").asArray();
            Http.get(assets.get(0).getString("browser_download_url")).timeout(0).block(response -> {
                info("Downloading Mindustry.jar...");
                Time.mark();

                mindustry.writeBytes(response.getResult());

                info("Mindustry.jar downloaded in @ms.", Time.elapsed());

                new ZipFi(mindustry).child("sprites").walk(fi -> {
                    info("Copying @ into @...", fi.name(), Vars.sprites.path());
                    if (fi.isDirectory()) fi.copyFilesTo(Vars.sprites);
                    else fi.copyTo(Vars.sprites);
                });

                Log.info("Unpacked @ files.", Vars.sprites.list().length);
            });
        });
    }

    private static void loadTextureDatas() {
        var data = new TextureAtlasData(Vars.sprites.child("sprites.aatls"), Vars.sprites, false);
        var images = new ObjectMap<AtlasPage, BufferedImage>();

        atlas = new TextureAtlas();

        data.getPages().each(page -> loadIgnoreErrors(() -> {
            page.texture = Texture.createEmpty(null);
            images.put(page, ImageIO.read(page.textureFile.file()));
        }));

        data.getRegions().each(region -> atlas.addRegion(region.name, new ImageRegion(region, images.get(region.page))));

        atlas.setErrorRegion("error");
        batch = new SchematicBatch();

        info("Loaded @ pages, @ regions.", data.getPages().size, data.getRegions().size);
    }

    private static void loadBlockColors() {
        var pixmap = new Pixmap(Vars.sprites.child("block_colors.png"));
        for (int i = 0; i < pixmap.width; i++) {
            var block = content.block(i);
            if (block.itemDrop != null) block.mapColor.set(block.itemDrop.color);
            else block.mapColor.rgba8888(pixmap.get(i, 0)).a(1f);
        }
        pixmap.dispose();

        info("Loaded @ block colors.", pixmap.width);
    }

    private static void loadItemEmojis() {
        Vars.emojis.putAll(
                scrap, 1041102555029176320L,
                copper, 1041102399214993489L,
                lead, 1041102538763669578L,
                graphite, 1041102536066744402L,
                coal, 1041102397809889361L,
                titanium, 1041102563254222858L,
                thorium, 1041102561719099523L,
                silicon, 1041102556312649748L,
                plastanium, 1041102550776160316L,
                phaseFabric, 1041102549408825404L,
                surgeAlloy, 1041102560213336145L,
                sporePod, 1041102558946672703L,
                sand, 1041102553682817074L,
                blastCompound, 1041102395888910446L,
                pyratite, 1041102552118345778L,
                metaglass, 1041102540424609882L,
                beryllium, 1041102393057747076L,
                tungsten, 1041102564906778724L,
                oxide, 1041102546711892019L,
                carbide, 1041102395888910446L,
                dormantCyst, 1041102532379951175L,
                fissileMatter, 1041102533764067338L

        );
    }

    private static void loadIgnoreErrors(UnsafeRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable ignored) {}
    }
}
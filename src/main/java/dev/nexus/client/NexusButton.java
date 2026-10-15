package dev.nexus.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.util.Identifier;

public final class NexusButton extends ClickableWidget {
    private static final Identifier UI_FONT = Identifier.of("minecraft", "uniform");
    private final Runnable action;

    public NexusButton(int x, int y, int width, int height, String label, Runnable action) {
        super(x, y, width, height, Text.literal(label).styled(style -> style.withFont(new StyleSpriteSource.Font(UI_FONT))));
        this.action = action;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = getX();
        int y = getY();
        int right = x + getWidth();
        int bottom = y + getHeight();
        boolean hover = isMouseOver(mouseX, mouseY);
        int fill = !active ? 0xFF17151B : hover ? 0xFF35202A : 0xFF211A23;
        context.fill(x, y, right, bottom, fill);
        context.fill(x, y, x + 2, bottom, hover ? 0xFFFF607C : 0xFF9A2C46);
        context.fill(x + 2, y, right, y + 1, 0xFF44303A);
        context.fill(x + 2, bottom - 1, right, bottom, 0xFF131117);
        int color = !active ? 0xFF77727D : hover ? 0xFFFFFFFF : 0xFFE8E3EB;
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(),
                x + getWidth() / 2, y + (getHeight() - 8) / 2, color);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        if (active && action != null) action.run();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}

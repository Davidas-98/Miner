import java.awt.*;
import java.util.concurrent.TimeUnit;

import ore.Rock;

import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

@ScriptManifest(author = "You", info = "My first script", name = "Miner", version = 0, logo = "")
public class Main extends Script {

    public String status = "";
    private long timeBegan;
    private long timeRan;
    private int coalCollected;
    private long startAmount;

    private final Font font = new Font("Calibri", Font.PLAIN, 12);
    Area tinandcopper = new Area(3221, 3149, 3231, 3143);
    Area coal = new Area(3143, 3153, 3147, 3148);
    Area bank = new Area(3092, 3245, 3094, 3241);
    public static final int[] pickaxes = new int[] { 1275, 1273, 1271, 1269, 1267, 1265 };
    public int randomsDismissed;

    @Override
    public void onStart() {
        log("Let's get started!");
        /*new ConditionalSleep(2500, 3000) {
            @Override
            public boolean condition() {
                return myPlayer().isVisible();
            }
        }.sleep();*/
        timeBegan = System.currentTimeMillis();
        getExperienceTracker().start(Skill.MINING);
        startAmount = getInventory().getAmount("Coal");
    }

    @Override
    public int onLoop() throws InterruptedException {
        if (getSkills().getDynamic(Skill.MINING) < 30){
            if (!walk(tinandcopper)) {
               drop();
               mine(Rock.TIN);
            } else {
                walk(tinandcopper);
            }
        } else if (getSkills().getDynamic(Skill.MINING) >= 30) {
            if (getInventory().getEmptySlotCount() == 0) {
                bank();
            } else if (!walk(coal)) {
                update();
                mine(Rock.COAL);
            } else{
                walk(coal);
            }
        }
        return random(100, 150);
    }

    @Override
    public void onExit() {
        log("byebye");
    }

    @Override
    public void onPaint(Graphics2D g) {
        drawMouse(g);

        //setfont
        g.setColor(Color.WHITE);
        g.setFont(font);

        timeRan = System.currentTimeMillis() - this.timeBegan;
        //status
        g.drawString(status, 420, 325);
        //info
        g.drawString("Coal miner ~ David", 15, 240);
        g.drawString("Ore mined: " + coalCollected, 15, 265);
        g.drawString("Time ran: " + ft(timeRan), 15, 280);
        g.drawString("Mining XP | p/h: " + getExperienceTracker().getGainedXP(Skill.MINING) + " | "
                + getExperienceTracker().getGainedXPPerHour(Skill.MINING), 15, 295);
        g.drawString("TTL: " + ft(getExperienceTracker().getTimeToLevel(Skill.MINING)), 15, 310);
        g.drawString("Levels gained | Current lvl: " + getExperienceTracker().getGainedLevels(Skill.MINING) + " | " + getSkills().getDynamic(Skill.MINING), 15, 325);
    }

    public void update(){
            long amount = getInventory().getAmount("Coal");;
            if(startAmount != amount) {
                if (amount == 0)
                {
                    startAmount = amount;
                } else {
                    coalCollected += amount - startAmount;
                    startAmount = amount;
                }
            }
    }

    public boolean drop () {
        if (getInventory().getEmptySlotCount() == 0) {
            status = "Dropping ore..";
            getInventory().dropAllExcept(pickaxes);
            return true;
        } else {
            return false;
        }
    }

    public void bank() {
        if(!walk(bank)){
            if (!getBank().isOpen()) {
                try {
                    getBank().open();
                    status = "Opening bank..";
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    log(e.toString());
                }
                new ConditionalSleep(2500, 3000) {
                    @Override
                    public boolean condition() {
                        return getBank().isOpen();
                    }
                }.sleep();
            } else {
                if(getSkills().getDynamic(Skill.MINING) >= 41 ){
                    if(getInventory().contains(1275)){
                    } else{
                        getBank().depositAll();
                        getBank().withdraw(1275,1);
                        getBank().close();
                    }
                }
                    getBank().depositAllExcept(pickaxes);
                    status = "Depositing ore..";
                    getBank().close();

            }
        } else {
            status = "Walking to bank..";
            walk(bank);
        }

    }

    public void mine (Rock a){
        if (getInventory().contains(pickaxes)) {
            RS2Object ore = getObjects().closest(obj -> a.hasOre(obj));

            if (ore != null && ore.isVisible()) {
                status = "Found ore..";
                if (ore.interact("Mine")) {
                    status = "Mining ore..";
                    new ConditionalSleep(2000) {
                        @Override
                        public boolean condition() {
                            return myPlayer().isAnimating() || !ore.exists();
                        }
                    }.sleep();
                }
            }
        } else {
            stop();
        }
        }

    public void drawMouse(Graphics graphics){
            ((Graphics2D) graphics).setRenderingHints(
                    new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
            Point pointer = mouse.getPosition();
            Graphics2D spinG = (Graphics2D) graphics.create();
            Graphics2D spinGRev = (Graphics2D) graphics.create();
            spinG.setColor(new Color(255, 255, 255));
            spinGRev.setColor(Color.cyan);
            spinG.rotate(System.currentTimeMillis() % 2000d / 2000d * (360d) * 2 * Math.PI / 180.0, pointer.x, pointer.y);
            spinGRev.rotate(System.currentTimeMillis() % 2000d / 2000d * (-360d) * 2 * Math.PI / 180.0, pointer.x, pointer.y);
            final int outerSize = 20;
            final int innerSize = 12;
            spinG.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            spinGRev.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            spinG.drawArc(pointer.x - (outerSize / 2), pointer.y - (outerSize / 2), outerSize, outerSize, 100, 75);
            spinG.drawArc(pointer.x - (outerSize / 2), pointer.y - (outerSize / 2), outerSize, outerSize, -100, 75);
            spinGRev.drawArc(pointer.x - (innerSize / 2), pointer.y - (innerSize / 2), innerSize, innerSize, 100, 75);
            spinGRev.drawArc(pointer.x - (innerSize / 2), pointer.y - (innerSize / 2), innerSize, innerSize, -100, 75);
    }



    public boolean walk(Area area){
        int randomRun = random(5);
        if (randomRun == 1) {
            getSettings().setRunning(getSettings().getRunEnergy() < 25 ? false : true);
        }
        if (dismissRandom()) {
            status = "Dismissing random..";
            while (myPlayer().isMoving()) {
                try {
                    sleep(random(600, 800));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        while (myPlayer().isAnimating()) {
            mouse.move(-1, -1);
        }
        if (myPlayer().isUnderAttack()) {
            getSettings().setRunning(true);
            getWalking().webWalk(area);
        }
        if (!getWalking().webWalk(area) ){
            status = "Walking to destination..";
            getWalking().webWalk(area);
        } else{
            return false;
        }
        return true;
    }

    private boolean dismissRandom() {
        for (NPC randomEvent : npcs.getAll()) {
            if (randomEvent == null || randomEvent.getInteracting() == null
                    || randomEvent.getInteracting() != myPlayer()) {
                continue;
            }
            if (randomEvent.hasAction("Dismiss")) {
                randomEvent.interact("Dismiss");
                randomsDismissed++;
                return true;
            }
        }
        return false;
    }

    //Displays time from milliseconds to hour:minute:seconds
    private String ft(long duration) {
        String res = "";
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
        if (days == 0) {
            res = (hours + ":" + minutes + ":" + seconds);
        } else {
            res = (days + ":" + hours + ":" + minutes + ":" + seconds);
        }
        return res;
    }
}
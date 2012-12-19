package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Weakness extends MobModifier
{
    public MM_Weakness(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Weakness";
    }
    
    public MM_Weakness(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Weakness";
        this.nextMod = prevMod;
    }
    
    @Override
    public int onHurt(DamageSource source, int damage)
    {
        if (source.getEntity() != null
        && (source.getEntity() instanceof EntityLiving))
        {
            ((EntityLiving)source.getEntity()).addPotionEffect(new PotionEffect(Potion.weakness.id, 120, 0));
        }
        
        return super.onHurt(source, damage);
    }
    
    @Override
    public int onAttack(EntityLiving entity, DamageSource source, int damage)
    {
        if (entity != null)
        {
            entity.addPotionEffect(new PotionEffect(Potion.weakness.id, 120, 0));
        }
        
        return super.onAttack(entity, source, damage);
    }
}

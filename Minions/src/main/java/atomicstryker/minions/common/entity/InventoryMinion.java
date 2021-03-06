package atomicstryker.minions.common.entity;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * Minion Inventory Class, has some standalone extras compared to a player
 * inventory but no armor
 * 
 * 
 * @author AtomicStryker
 */

public class InventoryMinion implements IInventory
{
    public ItemStack[] mainInventory = new ItemStack[24];
    public EntityMinion minion;
    public boolean inventoryChanged = false;

    public InventoryMinion(EntityMinion var1)
    {
        this.minion = var1;
    }

    private int storeItemStack(ItemStack par1ItemStack)
    {
        for (int i = 0; i < this.mainInventory.length; ++i)
        {
            if (this.mainInventory[i] != null && this.mainInventory[i].getItem() == par1ItemStack.getItem() && this.mainInventory[i].isStackable()
                    && this.mainInventory[i].getCount() < this.mainInventory[i].getMaxStackSize() && this.mainInventory[i].getCount() < this.getInventoryStackLimit()
                    && (!this.mainInventory[i].getHasSubtypes() || this.mainInventory[i].getItemDamage() == par1ItemStack.getItemDamage())
                    && ItemStack.areItemStackTagsEqual(this.mainInventory[i], par1ItemStack))
            {
                return i;
            }
        }

        return -1;
    }

    private int getFirstEmptyStack()
    {
        for (int i = 0; i < this.mainInventory.length; ++i)
        {
            if (this.mainInventory[i] == null)
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * This function stores as many items of an ItemStack as possible in a
     * matching slot and returns the quantity of left over Items.
     */
    private int storePartialItemStack(ItemStack par1ItemStack)
    {
        Item item = par1ItemStack.getItem();
        int i = par1ItemStack.getCount();
        int j;

        if (par1ItemStack.getMaxStackSize() == 1)
        {
            j = this.getFirstEmptyStack();

            if (j < 0)
            {
                return i;
            }
            else
            {
                if (this.mainInventory[j] == null)
                {
                    this.mainInventory[j] = par1ItemStack.copy();
                }

                return 0;
            }
        }
        else
        {
            j = this.storeItemStack(par1ItemStack);

            if (j < 0)
            {
                j = this.getFirstEmptyStack();
            }

            if (j < 0)
            {
                return i;
            }
            else
            {
                if (this.mainInventory[j] == null)
                {
                    this.mainInventory[j] = new ItemStack(item, 0, par1ItemStack.getItemDamage());

                    if (par1ItemStack.hasTagCompound())
                    {
                        this.mainInventory[j].setTagCompound((NBTTagCompound) par1ItemStack.getTagCompound().copy());
                    }
                }

                int k = i;

                if (i > this.mainInventory[j].getMaxStackSize() - this.mainInventory[j].getCount())
                {
                    k = this.mainInventory[j].getMaxStackSize() - this.mainInventory[j].getCount();
                }

                if (k > this.getInventoryStackLimit() - this.mainInventory[j].getCount())
                {
                    k = this.getInventoryStackLimit() - this.mainInventory[j].getCount();
                }

                if (k == 0)
                {
                    return i;
                }
                else
                {
                    i -= k;
                    this.mainInventory[j].grow(k);
                    this.mainInventory[j].setAnimationsToGo(5);
                    return i;
                }
            }
        }
    }

    public boolean addItemStackToInventory(final ItemStack par1ItemStack)
    {
        if (par1ItemStack != null && par1ItemStack.getCount() != 0 && par1ItemStack.getItem() != null)
        {
            try
            {
                int i;

                if (par1ItemStack.isItemDamaged())
                {
                    i = this.getFirstEmptyStack();

                    if (i >= 0)
                    {
                        this.mainInventory[i] = par1ItemStack.copy();
                        this.mainInventory[i].setAnimationsToGo(5);
                        par1ItemStack.setCount(0);
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    do
                    {
                        i = par1ItemStack.getCount();
                        par1ItemStack.setCount(storePartialItemStack(par1ItemStack));
                    }
                    while (par1ItemStack.getCount() > 0 && par1ItemStack.getCount() < i);

                    return par1ItemStack.getCount() < i;
                }
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being added");
                crashreportcategory.addCrashSection("Item ID", Item.getIdFromItem(par1ItemStack.getItem()));
                crashreportcategory.addCrashSection("Item data", par1ItemStack.getItemDamage());
                throw new ReportedException(crashreport);
            }
        }
        else
        {
            return false;
        }
    }

    public NBTTagList writeToNBT(NBTTagList par1NBTTagList)
    {
        NBTTagCompound nbttagcompound;

        for (int i = 0; i < this.mainInventory.length; ++i)
        {
            if (this.mainInventory[i] != null)
            {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                this.mainInventory[i].writeToNBT(nbttagcompound);
                par1NBTTagList.appendTag(nbttagcompound);
            }
        }

        return par1NBTTagList;
    }

    public void readFromNBT(NBTTagList par1NBTTagList)
    {
        this.mainInventory = new ItemStack[24];

        for (int i = 0; i < par1NBTTagList.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = par1NBTTagList.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            ItemStack itemstack = new ItemStack(nbttagcompound);

            if (itemstack != null)
            {
                if (j >= 0 && j < this.mainInventory.length)
                {
                    this.mainInventory[j] = itemstack;
                }
            }
        }
    }

    public boolean consumeInventoryItem(Object item)
    {
        int i = this.getFirstSlotWithItem(item);

        if (i < 0)
        {
            return false;
        }
        else
        {
            mainInventory[i].shrink(1);
            if (mainInventory[i].getCount() <= 0)
            {
                this.mainInventory[i] = null;
            }

            return true;
        }
    }

    private int getFirstSlotWithItem(Object item)
    {
        for (int i = 0; i < this.mainInventory.length; ++i)
        {
            if (this.mainInventory[i] != null)
            {
                if (mainInventory[i] == item || mainInventory[i].getItem() == item)
                {
                    return i;
                }
            }
        }

        return -1;
    }

    public void dropAllItems()
    {
        int var1;
        for (var1 = 0; var1 < this.mainInventory.length; ++var1)
        {
            if (this.mainInventory[var1] != null)
            {
                this.minion.dropMinionItemWithRandomChoice(this.mainInventory[var1]);
                this.mainInventory[var1] = null;
            }
        }
        minion.inventoryFull = false;
    }

    public boolean containsItems()
    {
        return this.getFirstEmptyStack() != 0;
    }

    public void putAllItemsToInventory(IInventory returnChestOrInventory)
    {
        int var1;
        for (var1 = 0; var1 < this.mainInventory.length; ++var1)
        {
            if (this.mainInventory[var1] != null)
            {
                if (addItemStackToInventory(returnChestOrInventory, this.mainInventory[var1]))
                {
                    this.mainInventory[var1] = null;
                }
                else if (returnChestOrInventory instanceof TileEntityChest)
                {
                    TileEntityChest returnChest = (TileEntityChest) returnChestOrInventory;
                    if (returnChest.adjacentChestXNeg != null && addItemStackToInventory(returnChest.adjacentChestXNeg, this.mainInventory[var1])
                            || returnChest.adjacentChestXPos != null && addItemStackToInventory(returnChest.adjacentChestXPos, this.mainInventory[var1])
                            || returnChest.adjacentChestZNeg != null && addItemStackToInventory(returnChest.adjacentChestZNeg, this.mainInventory[var1])
                            || returnChest.adjacentChestZPos != null && addItemStackToInventory(returnChest.adjacentChestZPos, this.mainInventory[var1]))
                    {
                        this.mainInventory[var1] = null;
                    }
                }
                else
                {
                    this.dropAllItems();
                    return;
                }
            }
        }
        minion.inventoryFull = false;
    }

    private boolean addItemStackToInventory(IInventory inv, ItemStack item)
    {
        int index;
        if (item.isItemStackDamageable() && item.isItemDamaged())
        {
            index = getInvFirstEmptyStack(inv);
            if (index >= 0)
            {
                inv.setInventorySlotContents(index, item.copy());
                inv.getStackInSlot(index).setAnimationsToGo(5);
                item.setCount(0);
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            do
            {
                index = item.getCount();
                item.setCount(storePartialItemStackInChest(inv, item));
            }
            while (item.getCount() > 0 && item.getCount() < index);

            return item.getCount() < index;
        }
    }

    private int storePartialItemStackInChest(IInventory inv, ItemStack item)
    {
        Item itemID = item.getItem();
        int stacksize = item.getCount();
        int index;
        if (item.getMaxStackSize() == 1)
        {
            index = getInvFirstEmptyStack(inv);
            if (index < 0)
            {
                return stacksize;
            }
            else
            {
                if (inv.getStackInSlot(index) == ItemStack.EMPTY)
                {
                    inv.setInventorySlotContents(index, item.copy());
                }

                return 0;
            }
        }
        else
        {
            index = this.storeItemStackInInv(inv, item);
            if (index < 0)
            {
                index = getInvFirstEmptyStack(inv);
            }

            if (index < 0)
            {
                return stacksize;
            }
            else
            {
                if (inv.getStackInSlot(index) == ItemStack.EMPTY)
                {
                    inv.setInventorySlotContents(index, new ItemStack(itemID, 0, item.getItemDamage()));
                    if (item.hasTagCompound())
                    {
                        inv.getStackInSlot(index).setTagCompound((NBTTagCompound) item.getTagCompound().copy());
                    }
                }

                int remainingsize = stacksize;
                if (stacksize > inv.getStackInSlot(index).getMaxStackSize() - inv.getStackInSlot(index).getCount())
                {
                    remainingsize = inv.getStackInSlot(index).getMaxStackSize() - inv.getStackInSlot(index).getCount();
                }

                if (remainingsize > inv.getInventoryStackLimit() - inv.getStackInSlot(index).getCount())
                {
                    remainingsize = inv.getInventoryStackLimit() - inv.getStackInSlot(index).getCount();
                }

                if (remainingsize == 0)
                {
                    return stacksize;
                }
                else
                {
                    stacksize -= remainingsize;
                    inv.getStackInSlot(index).grow(remainingsize);
                    inv.getStackInSlot(index).setAnimationsToGo(5);
                    return stacksize;
                }
            }
        }
    }

    private int getInvFirstEmptyStack(IInventory inv)
    {
        for (int index = 0; index < inv.getSizeInventory(); ++index)
        {
            if (inv.getStackInSlot(index) == ItemStack.EMPTY)
            {
                return index;
            }
        }

        return -1;
    }

    private int storeItemStackInInv(IInventory inv, ItemStack item)
    {
        for (int index = 0; index < inv.getSizeInventory(); ++index)
        {
            if (inv.getStackInSlot(index) != ItemStack.EMPTY && inv.getStackInSlot(index).getItem() == item.getItem() && inv.getStackInSlot(index).isStackable()
                    && inv.getStackInSlot(index).getCount() < inv.getStackInSlot(index).getMaxStackSize() && inv.getStackInSlot(index).getCount() < inv.getInventoryStackLimit()
                    && (!inv.getStackInSlot(index).getHasSubtypes() || inv.getStackInSlot(index).getItemDamage() == item.getItemDamage()))
            {
                return index;
            }
        }

        return -1;
    }

    @Override
    public ItemStack decrStackSize(int par1, int par2)
    {
        ItemStack[] aitemstack = this.mainInventory;

        if (par1 >= this.mainInventory.length)
        {
            return ItemStack.EMPTY;
        }

        if (aitemstack[par1] != ItemStack.EMPTY)
        {
            ItemStack itemstack;

            if (aitemstack[par1].getCount() <= par2)
            {
                itemstack = aitemstack[par1];
                aitemstack[par1] = ItemStack.EMPTY;
                return itemstack;
            }
            else
            {
                itemstack = aitemstack[par1].splitStack(par2);

                if (aitemstack[par1].getCount() == 0)
                {
                    aitemstack[par1] = ItemStack.EMPTY;
                }

                return itemstack;
            }
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int par1)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
    {
        ItemStack[] aitemstack = this.mainInventory;

        if (par1 < aitemstack.length)
        {
            aitemstack[par1] = par2ItemStack;
        }
    }

    @Override
    public int getSizeInventory()
    {
        return this.mainInventory.length + 4;
    }

    @Override
    public ItemStack getStackInSlot(int par1)
    {
        return mainInventory[par1];
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public void markDirty()
    {
        this.inventoryChanged = true;
    }

    @Override
    public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack)
    {
        return true;
    }

    @Override
    public String getName()
    {
        return "Minion Inventory";
    }

    @Override
    public boolean hasCustomName()
    {
        return true;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentTranslation(getName());
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public void clear()
    {
    }

    @Override
    public boolean isEmpty()
    {
        return !containsItems();
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        return false;
    }

}

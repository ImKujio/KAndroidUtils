package me.kujio.android.kandroidutils

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.kujio.android.kandroidutils.databinding.ItemLoadMoreBinding

fun EditText.showKeyboard() {
    isFocusable = true
    isFocusableInTouchMode = true
    requestFocus()
    if (context !is Activity) return
    val imm = context.getSystemService(InputMethodManager::class.java) as InputMethodManager
    imm.showSoftInput(this, 0)
}

@BindingAdapter("android:kAdapter")
fun setKAdapter(
    recyclerView: RecyclerView, simpleAdapter: KRecyclerAdapter?
) {
    if (simpleAdapter == null) return
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context).apply {
        orientation = LinearLayoutManager.VERTICAL
    }
    recyclerView.adapter = simpleAdapter
}

class KViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

open class KRecyclerAdapter(
    private val resId: (pos: Int) -> Pair<Int, Int>,
    private val count: () -> Int,
    private val onBinding: KRecyclerAdapter.(view: RecyclerView, binding: ViewDataBinding, pos: Int) -> Unit
) : RecyclerView.Adapter<KViewHolder>() {
    protected lateinit var recyclerView: RecyclerView
    protected val resIds = ArrayList<Int>()
    protected var curItemCount = 0

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KViewHolder {
        return KViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), resIds[viewType], parent, false
            )
        )
    }

    override fun getItemViewType(position: Int): Int {
        val pair = resId(position)
        var type = resIds.indexOf(pair.first)
        if (type == -1) {
            resIds.add(pair.first)
            recyclerView.recycledViewPool.setMaxRecycledViews(pair.first, pair.second)
            type = resIds.size - 1
        }
        return type
    }

    override fun onBindViewHolder(holder: KViewHolder, position: Int) {
        onBinding(recyclerView, holder.binding, position)
    }

    override fun getItemCount(): Int {
        curItemCount = count()
        return curItemCount
    }

    open fun notifyReset(){
        val lastCount = curItemCount
        val curCount = itemCount
        if (lastCount == 0 && curCount == 0) return
        if (lastCount > 0) notifyItemRangeRemoved(0,lastCount)
        if (curCount > 0) notifyItemRangeInserted(0,curCount)
        recyclerView.scrollTo(0,0)
    }

    open fun notifyAppend(){
        val lastCount = curItemCount
        val curCount = itemCount
        if (lastCount == 0 && curCount == 0) return
        if (lastCount == curCount) return
        val scrollY = recyclerView.scrollY
        if (curCount > lastCount) notifyItemRangeInserted(lastCount,curCount)
        recyclerView.scrollTo(0,scrollY)
    }
}

class KMoreRecyclerAdapter(
    resId: (pos: Int) -> Pair<Int, Int>,
    count: () -> Int,
    private val load: suspend (adapter : KMoreRecyclerAdapter) -> Unit,
    private val more: () -> Boolean,
    onBinding: KRecyclerAdapter.(view: RecyclerView, binding: ViewDataBinding, pos: Int) -> Unit
):KRecyclerAdapter(resId,count,onBinding){
    private val loadMoreRes = R.layout.item_load_more
    private var isLoading = false
    private lateinit var scope: CoroutineScope
    private lateinit var loadMoreViewHolder: KViewHolder

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        scope = CoroutineScope(Dispatchers.Main)
        recyclerView.recycledViewPool.setMaxRecycledViews(-1, 1)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = recyclerView.layoutManager?.childCount ?: 0
                val totalItemCount = recyclerView.layoutManager?.itemCount ?: 0
                val firstVisibleItemPosition = (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: 0
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    loadMoreViewHolder.binding.let { binding ->
                        binding as ItemLoadMoreBinding
                        if (more()) loadMore()
                        else binding.state = 2
                    }
                }
            }
        })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        scope.cancel()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    private fun loadMore() {
        if (isLoading) return
        isLoading = true
        loadMoreViewHolder.binding.let { binding ->
            binding as ItemLoadMoreBinding
            binding.state = 1
            scope.launch {
                try {
                    load(this@KMoreRecyclerAdapter)
                    isLoading = false
                } catch (_: Throwable) {
                    binding.state = 3
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KViewHolder {
        return KViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                if (viewType == -1) loadMoreRes else resIds[viewType],
                parent,
                false
            )
        ).also {
            if (viewType == -1) loadMoreViewHolder = it
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == itemCount - 1) return -1
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: KViewHolder, position: Int) {
        if (holder.binding is ItemLoadMoreBinding) {
            if (more()) loadMore()
            else holder.binding.state = 2
            holder.binding.load = View.OnClickListener {
                loadMore()
            }
        } else {
            super.onBindViewHolder(holder, position)
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun notifyReset() {
        val lastCount = curItemCount
        val curCount = itemCount - 1
        if (lastCount == 0 && curCount == 0) return
        if (lastCount > 0) notifyItemRangeRemoved(0,lastCount)
        if (curCount > 0) notifyItemRangeRemoved(0,curCount)
        recyclerView.scrollTo(0,0)
    }

    override fun notifyAppend(){
        val lastCount = curItemCount
        val curCount = itemCount - 1
        if (lastCount == 0 && curCount == 0) return
        if (lastCount == curCount) return
        val scrollY = recyclerView.scrollY
        if (curCount > lastCount) notifyItemRangeInserted(lastCount, curCount - lastCount)
        recyclerView.scrollTo(0,scrollY)
    }
}